package org.sarden.core.plant

// TODO: Use wrapper (maybe zio-nio)
import java.nio.file.{Files, Paths}

import zio.*
import zio.json.*

import org.sarden.core.IdGenerator
import org.sarden.core.plant.internal.{LivePlantRepo, PlantRepo}
import org.sarden.core.tx.*

trait PlantService:
  def createPlant(name: PlantName, details: PlantDetails): UIO[PlantId]
  def deletePlant(id: PlantId): UIO[Unit]
  def getPlant(id: PlantId): UIO[Option[Plant]]
  def searchPlants(filters: SearchPlantFilters): UIO[Vector[Plant]]
  def backupPlants(): UIO[Unit]

object PlantService:
  val live: URLayer[Tx.Runner & IdGenerator, PlantService] =
    ZLayer.fromZIO {
      for
        tx <- ZIO.service[Tx.Runner]
        idGenerator <- ZIO.service[IdGenerator]
      yield LivePlantService(LivePlantRepo(idGenerator), tx)
    }

class LivePlantService(repo: PlantRepo, tx: Tx.Runner) extends PlantService:
  override def createPlant(
      name: PlantName,
      details: PlantDetails,
  ): UIO[PlantId] =
    tx.runOrDie(repo.createPlant(name, details)) <* backupPlants()

  override def deletePlant(id: PlantId): UIO[Unit] =
    ZIO.attempt(???).orDie

  override def getPlant(id: PlantId): UIO[Option[Plant]] =
    ZIO.attempt(???).orDie

  override def searchPlants(filters: SearchPlantFilters): UIO[Vector[Plant]] =
    tx.runOrDie(repo.searchPlants(filters))

  override def backupPlants(): UIO[Unit] =
    tx.runOrDie(repo.searchPlants(SearchPlantFilters.empty))
      .flatMap { plants =>
        ZIO.attemptBlocking {
          Files.writeString(Paths.get("plants.json"), plants.toJson)
        }.orDie
      }
      .unit
