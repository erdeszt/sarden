package org.sarden.core.plant.internal

import scala.io.Source

import cats.data.NonEmptyList
import cats.instances.vector.given
import doobie.util.fragment.Fragment
import doobie.util.fragments
import zio.*
import zio.json.*

import org.sarden.core.IdGenerator
import org.sarden.core.SystemErrors.DataFormatError
import org.sarden.core.mapping.given
import org.sarden.core.plant.*
import org.sarden.core.tx.*

private[plant] trait PlantRepo:
  def searchPlants(filter: SearchPlantFilters): URIO[Tx, Vector[Plant]]
  def createPlant(
      name: PlantName,
      details: PlantDetails,
  ): URIO[Tx, PlantId]
  def loadPresetData: URIO[Tx, Unit]
  def getPlantsByIds(ids: NonEmptyList[PlantId]): URIO[Tx, Vector[Plant]]

case class LivePlantRepo(idGenerator: IdGenerator) extends PlantRepo:

  override def searchPlants(
      filter: SearchPlantFilters,
  ): URIO[Tx, Vector[Plant]] =
    Tx {
      sql"SELECT id, name FROM plant".queryThrough[PlantDTO, Plant].to[Vector]
    }

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

  override def loadPresetData: URIO[Tx, Unit] =
    for
      rawPlants <- ZIO.scoped:
        ZIO
          .fromAutoCloseable(ZIO.attempt(Source.fromResource("plants.json")))
          .map(_.getLines.mkString("\n"))
          .orDie
      plants <- ZIO
        .fromEither(rawPlants.fromJson[Vector[CreatePlantDTO]])
        .mapError(DataFormatError(_))
        .orDie
      _ <- ZIO.foreachDiscard(plants): plant =>
        createPlant(PlantName(plant.name), PlantDetails())
    yield ()

  override def getPlantsByIds(
      ids: NonEmptyList[PlantId],
  ): URIO[Tx, Vector[Plant]] =
    Tx {
      (fr"SELECT id, name FROM plant WHERE " ++ fragments.in(fr"id", ids))
        .queryThrough[PlantDTO, Plant]
        .to[Vector]
    }
