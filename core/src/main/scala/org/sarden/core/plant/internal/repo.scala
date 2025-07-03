package org.sarden.core.plant.internal

import scala.io.Source

import cats.data.{NonEmptyList, NonEmptySet}
import doobie.util.fragments
import io.scalaland.chimney.dsl.*
import zio.*
import zio.json.*

import org.sarden.core.*
import org.sarden.core.mapping.given
import org.sarden.core.plant.*
import org.sarden.core.tx.*

private[internal] case class PresetDataFormatError(message: String)
    extends InternalError(s"Preset data format error: ${message}")

private[internal] case class InvalidCompanionBenefitFormatError(raw: String)
    extends InternalError(s"Invalid CompanioinBenefit format: ${raw}")

private[plant] trait PlantRepo:
  def searchPlants(filter: SearchPlantFilters): URIO[Tx, Vector[Plant]]
  def createPlant(
      name: PlantName,
      details: PlantDetails,
  ): URIO[Tx, PlantId]
  def deletePlant(id: PlantId): URIO[Tx, Unit]
  def loadPresetData: URIO[Tx, Unit]
  def getPlantsByIds(ids: NonEmptyList[PlantId]): URIO[Tx, Vector[Plant]]
  def getPlant(id: PlantId): URIO[Tx, Option[Plant]]
  def createVariety(plantId: PlantId, name: VarietyName): URIO[Tx, VarietyId]
  def getVarietiesOfPlant(plantId: PlantId): URIO[Tx, Vector[Variety[PlantId]]]
  def createCompanion(
      companionPlantId: PlantId,
      targetPlantId: PlantId,
      benefits: NonEmptySet[CompanionBenefit],
  ): URIO[Tx, CompanionId]
  def getCompanionsOfPlant(
      targetPlantId: PlantId,
  ): URIO[Tx, Vector[Companion[PlantId]]]
  def getCompanionByPlants(
      companionPlantId: PlantId,
      targetPlantId: PlantId,
  ): URIO[Tx, Option[Companion[PlantId]]]
  def getCompanionRelations(
      plantId: PlantId,
  ): URIO[Tx, Vector[Companion[PlantId]]]
  def deleteCompanion(
      id: CompanionId,
  ): URIO[Tx, Unit]

case class LivePlantRepo(idGenerator: IdGenerator) extends PlantRepo:

  override def searchPlants(
      filter: SearchPlantFilters,
  ): URIO[Tx, Vector[Plant]] =
    Tx:
      sql"SELECT id, name FROM plant".queryThrough[PlantDTO, Plant].to[Vector]

  override def createPlant(
      name: PlantName,
      details: PlantDetails,
  ): URIO[Tx, PlantId] =
    for
      id <- idGenerator.next()
      now <- zio.Clock.instant
      _ <- Tx:
        sql"""INSERT INTO plant
             |(id, name, created_at)
             |VALUES
             |(${id}, ${name}, ${now.getEpochSecond})""".stripMargin.update.run
    yield PlantId(id)

  override def deletePlant(id: PlantId): URIO[Tx, Unit] =
    Tx {
      sql"DELETE FROM plant WHERE id = ${id}".update.run
    }.unit

  override def loadPresetData: URIO[Tx, Unit] =
    for
      rawPlants <- ZIO.scoped:
        ZIO
          .fromAutoCloseable(ZIO.attempt(Source.fromResource("plants.json")))
          .map(_.getLines.mkString("\n"))
          .orDie
      plants <- ZIO
        .fromEither(rawPlants.fromJson[Vector[CreatePlantDTO]])
        .mapError(PresetDataFormatError.apply)
        .orDie
      _ <- ZIO.foreachDiscard(plants): plant =>
        createPlant(PlantName(plant.name), PlantDetails())
    yield ()

  override def getPlantsByIds(
      ids: NonEmptyList[PlantId],
  ): URIO[Tx, Vector[Plant]] =
    Tx:
      (fr"SELECT id, name FROM plant WHERE " ++ fragments.in(fr"id", ids))
        .queryThrough[PlantDTO, Plant]
        .to[Vector]

  override def getPlant(id: PlantId): URIO[Tx, Option[Plant]] =
    Tx:
      sql"SELECT id, name FROM plant WHERE id = ${id}"
        .queryThrough[PlantDTO, Plant]
        .option

  override def createVariety(
      plantId: PlantId,
      name: VarietyName,
  ): URIO[Tx, VarietyId] =
    for
      varietyId <- idGenerator.next()
      now <- zio.Clock.instant.map(_.getEpochSecond)
      _ <- Tx:
        sql"""INSERT INTO variety
             |(id, plant_id, name, created_at)
             |VALUES
             |(${varietyId}, ${plantId}, ${name}, ${now})""".stripMargin.update.run
    yield VarietyId(varietyId)

  override def getVarietiesOfPlant(
      plantId: PlantId,
  ): URIO[Tx, Vector[Variety[PlantId]]] =
    Tx:
      sql"SELECT id, plant_id, name FROM variety WHERE plant_id = ${plantId}"
        .queryTransformPartial[VarietyDTO](
          _.intoPartial[Variety[PlantId]]
            .withFieldRenamed(_.plantId, _.plant)
            .transform,
        )
        .to[Vector]

  override def createCompanion(
      companionPlantId: PlantId,
      targetPlantId: PlantId,
      benefits: NonEmptySet[CompanionBenefit],
  ): URIO[Tx, CompanionId] =
    for
      companionId <- idGenerator.next()
      now <- zio.Clock.instant.map(_.getEpochSecond)
      benefitsDTO = BenefitsDTO.fromBenefits(benefits.toSortedSet)
      _ <- Tx:
        sql"""INSERT INTO companion
             |(id, companion_plant_id, target_plant_id, benefits, created_at)
             |VALUES
             |(${companionId}, ${companionPlantId}, ${targetPlantId}, ${benefitsDTO}, ${now})""".stripMargin.update.run
    yield CompanionId(companionId)

  override def getCompanionsOfPlant(
      targetPlantId: PlantId,
  ): URIO[Tx, Vector[Companion[PlantId]]] =
    Tx:
      sql"SELECT id, companion_plant_id, target_plant_id, benefits FROM companion WHERE target_plant_id = ${targetPlantId}"
        .queryTransformPartial[CompanionDTO](
          _.intoPartial[Companion[PlantId]]
            .withFieldRenamed(_.companionPlantId, _.companionPlant)
            .withFieldRenamed(_.targetPlantId, _.targetPlant)
            .withFieldRenamed(_.benefits.benefits, _.benefits)
            .transform,
        )
        .to[Vector]

  override def getCompanionByPlants(
      companionPlantId: PlantId,
      targetPlantId: PlantId,
  ): URIO[Tx, Option[Companion[PlantId]]] =
    Tx:
      sql"""SELECT id, companion_plant_id, target_plant_id, benefits
           |FROM companion
           |WHERE target_plant_id = ${targetPlantId}
           |  AND companion_plant_id = ${companionPlantId}""".stripMargin
        .queryTransformPartial[CompanionDTO](
          _.intoPartial[Companion[PlantId]]
            .withFieldRenamed(_.companionPlantId, _.companionPlant)
            .withFieldRenamed(_.targetPlantId, _.targetPlant)
            .withFieldRenamed(_.benefits.benefits, _.benefits)
            .transform,
        )
        .option

  override def getCompanionRelations(
      plantId: PlantId,
  ): URIO[Tx, Vector[Companion[PlantId]]] =
    Tx:
      sql"""SELECT id, companion_plant_id, target_plant_id, benefits
           |FROM companion
           |WHERE target_plant_id = ${plantId}
           |   OR companion_plant_id = ${plantId}""".stripMargin
        .queryTransformPartial[CompanionDTO](
          _.intoPartial[Companion[PlantId]]
            .withFieldRenamed(_.companionPlantId, _.companionPlant)
            .withFieldRenamed(_.targetPlantId, _.targetPlant)
            .withFieldRenamed(_.benefits.benefits, _.benefits)
            .transform,
        )
        .to[Vector]

  override def deleteCompanion(
      id: CompanionId,
  ): URIO[Tx, Unit] =
    Tx {
      sql"DELETE FROM companion WHERE id = ${id}".update.run
    }.unit
