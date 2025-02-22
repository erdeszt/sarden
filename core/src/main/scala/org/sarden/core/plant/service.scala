package org.sarden.core.plant

import cats.data.{NonEmptyList, NonEmptySet}
import zio.*

import org.sarden.core.plant.internal.{LivePlantRepo, PlantRepo}
import org.sarden.core.tx.*
import org.sarden.core.{IdGenerator, InvalidRequestError}

case class MissingPlantError(id: PlantId)
    extends InvalidRequestError(s"Missing plant: ${id}") derives CanEqual

case class SelfCompanionError(id: PlantId)
    extends InvalidRequestError(
      s"Plants can't companions of themselves, attempted with plant: ${id}",
    ) derives CanEqual

case class CompanionAlreadyExistsError(id: CompanionId)
    extends InvalidRequestError(s"Companion already exists: ${id}")
    derives CanEqual

case class CantDeletePlantWithCompanionRelationsError(id: PlantId)
    extends InvalidRequestError(
      s"Can't delete plant that has companion relations: ${id}",
    ) derives CanEqual

case class CompanionRelationNotFoundError(
    companionPlantId: PlantId,
    targetPlantId: PlantId,
) extends InvalidRequestError(
      s"Companion relation not found between ${companionPlantId} and ${targetPlantId}",
    ) derives CanEqual

trait PlantService:
  def createPlant(name: PlantName, details: PlantDetails): UIO[PlantId]
  def deletePlant(
      id: PlantId,
  ): IO[MissingPlantError | CantDeletePlantWithCompanionRelationsError, Unit]
  def getPlant(id: PlantId): IO[MissingPlantError, Plant]
  def searchPlants(filters: SearchPlantFilters): UIO[Vector[Plant]]
  def getPlantsByIds(ids: NonEmptyList[PlantId]): UIO[Map[PlantId, Plant]]
  def createVariety(
      plantId: PlantId,
      name: VarietyName,
  ): IO[MissingPlantError, VarietyId]
  def getVarietiesOfPlant(
      plantId: PlantId,
  ): IO[MissingPlantError, Vector[Variety[PlantId]]]
  def loadPresetData: UIO[Unit]
  def createCompanion(
      companionPlantId: PlantId,
      targetPlantId: PlantId,
      benefits: NonEmptySet[CompanionBenefit],
  ): IO[
    MissingPlantError | SelfCompanionError | CompanionAlreadyExistsError,
    CompanionId,
  ]
  def getCompanionsOfPlant(
      targetPlantId: PlantId,
  ): IO[MissingPlantError, Vector[Companion[Plant]]]
  def deleteCompanion(
      companionPlantId: PlantId,
      targetPlantId: PlantId,
  ): IO[MissingPlantError, Unit]

object PlantService:
  val live: URLayer[Tx.Runner & IdGenerator, PlantService] =
    ZLayer.fromZIO:
      for
        tx <- ZIO.service[Tx.Runner]
        idGenerator <- ZIO.service[IdGenerator]
      yield LivePlantService(LivePlantRepo(idGenerator), tx)

class LivePlantService(repo: PlantRepo, tx: Tx.Runner) extends PlantService:
  override def createPlant(
      name: PlantName,
      details: PlantDetails,
  ): UIO[PlantId] =
    tx.runOrDie(repo.createPlant(name, details))

  override def deletePlant(
      id: PlantId,
  ): IO[MissingPlantError | CantDeletePlantWithCompanionRelationsError, Unit] =
    tx.runOrDie:
      for
        _ <- repo.getPlant(id).someOrFail(MissingPlantError(id))
        companionRelations <- repo.getCompanionRelations(id)
        _ <- ZIO.when(companionRelations.nonEmpty):
          ZIO.fail(CantDeletePlantWithCompanionRelationsError(id))
        _ <- repo.deletePlant(id)
      yield ()

  override def getPlant(id: PlantId): IO[MissingPlantError, Plant] =
    tx.runOrDie(repo.getPlant(id)).someOrFail(MissingPlantError(id))

  override def searchPlants(filters: SearchPlantFilters): UIO[Vector[Plant]] =
    tx.runOrDie(repo.searchPlants(filters))

  override def createVariety(
      plantId: PlantId,
      name: VarietyName,
  ): IO[MissingPlantError, VarietyId] =
    tx.runOrDie:
      for
        _ <- repo.getPlant(plantId).someOrFail(MissingPlantError(plantId))
        varietyId <- repo.createVariety(plantId, name)
      yield varietyId

  override def getPlantsByIds(
      ids: NonEmptyList[PlantId],
  ): UIO[Map[PlantId, Plant]] =
    tx.runOrDie(repo.getPlantsByIds(ids))
      .map(_.map(plant => (plant.id, plant)).toMap)

  override def loadPresetData: UIO[Unit] =
    tx.runOrDie(repo.loadPresetData)

  override def getVarietiesOfPlant(
      plantId: PlantId,
  ): IO[MissingPlantError, Vector[Variety[PlantId]]] =
    tx.runOrDie:
      for
        _ <- repo.getPlant(plantId).someOrFail(MissingPlantError(plantId))
        varieties <- repo.getVarietiesOfPlant(plantId)
      yield varieties

  override def createCompanion(
      companionPlantId: PlantId,
      targetPlantId: PlantId,
      benefits: NonEmptySet[CompanionBenefit],
  ): IO[
    MissingPlantError | SelfCompanionError | CompanionAlreadyExistsError,
    CompanionId,
  ] =
    tx.runOrDie:
      for
        _ <- ZIO.when(companionPlantId == targetPlantId):
          ZIO.fail(SelfCompanionError(companionPlantId))
        _ <- repo
          .getPlant(companionPlantId)
          .someOrFail(MissingPlantError(companionPlantId))
          .zipPar:
            repo
              .getPlant(targetPlantId)
              .someOrFail(MissingPlantError(targetPlantId))
        existingCompanion <- repo.getCompanionByPlants(
          companionPlantId,
          targetPlantId,
        )
        _ <- ZIO.whenCase(existingCompanion):
          case Some(companion) =>
            ZIO.fail(CompanionAlreadyExistsError(companion.id))
        companionId <- repo.createCompanion(
          companionPlantId,
          targetPlantId,
          benefits,
        )
      yield companionId

  override def getCompanionsOfPlant(
      targetPlantId: PlantId,
  ): IO[MissingPlantError, Vector[Companion[Plant]]] =
    tx.runOrDie:
      for
        targetPlant <- getPlant(targetPlantId)
        companions <- repo.getCompanionsOfPlant(targetPlantId)
        companionPlantLookup <- ZIO
          .foreach(
            NonEmptyList.fromFoldable(companions.map(_.companionPlant)),
          ) { companionIds =>
            repo.getPlantsByIds(companionIds)
          }
          .someOrElse(Vector.empty)
          .map(
            _.map(companionPlant => (companionPlant.id, companionPlant)).toMap,
          )
        companionsWithPlants <- ZIO.foreach(companions) { companion =>
          ZIO
            .fromOption(companionPlantLookup.get(companion.companionPlant))
            .mapBoth(
              _ => MissingPlantError(companion.companionPlant),
              { companionPlant =>
                companion.copy(
                  companionPlant = companionPlant,
                  targetPlant = targetPlant,
                )
              },
            )
        }
      yield companionsWithPlants

  override def deleteCompanion(
      companionPlantId: PlantId,
      targetPlantId: PlantId,
  ): IO[MissingPlantError, Unit] =
    tx.runOrDie:
      for
        companion <- repo
          .getCompanionByPlants(
            companionPlantId,
            targetPlantId,
          )
          .someOrFail(
            CompanionRelationNotFoundError(companionPlantId, targetPlantId),
          )
          .orDie
        _ <- repo.deleteCompanion(companion.id)
      yield ()
