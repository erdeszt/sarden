package org.sarden.core.plant

import cats.data.NonEmptyList
import zio.*

import org.sarden.core.plant.internal.{LivePlantRepo, PlantRepo}
import org.sarden.core.tx.*
import org.sarden.core.{IdGenerator, InvalidRequestError}

case class MissingPlantError(id: PlantId)
    extends InvalidRequestError(s"Missing plant: ${id}")

trait PlantService:
  def createPlant(name: PlantName, details: PlantDetails): UIO[PlantId]
  def deletePlant(id: PlantId): UIO[Unit]
  def getPlant(id: PlantId): IO[MissingPlantError, Plant]
  def searchPlants(filters: SearchPlantFilters): UIO[Vector[Plant]]
  def getPlantsByIds(ids: NonEmptyList[PlantId]): UIO[Map[PlantId, Plant]]
  def createVariety(
      plantId: PlantId,
      name: VarietyName,
  ): IO[MissingPlantError, VarietyId]
  def getVarietiesOfPlant(
      plantId: PlantId,
  ): IO[MissingPlantError, Vector[Variety]]
  def loadPresetData: UIO[Unit]

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

  override def deletePlant(id: PlantId): UIO[Unit] =
    tx.runOrDie(repo.deletePlant(id))

  // TODO: Use UserNotFoundError instead of option at the service level
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
  ): IO[MissingPlantError, Vector[Variety]] =
    tx.runOrDie:
      for
        _ <- repo.getPlant(plantId).someOrFail(MissingPlantError(plantId))
        varieties <- repo.getVarietiesOfPlant(plantId)
      yield varieties
