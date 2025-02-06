package org.sarden.core.domain.plant

import java.nio.file.{Files, Paths}

import io.github.gaelrenoux.tranzactio.*
import io.github.gaelrenoux.tranzactio.doobie.*
import zio.*
import zio.json.*

import org.sarden.core.IdGenerator
import org.sarden.core.domain.plant.internal.*

trait PlantService:
  def createPlant(name: PlantName, details: PlantDetails): UIO[PlantId]
  def deletePlant(id: PlantId): UIO[Unit]
  def getPlant(id: PlantId): UIO[Option[Plant]]
  def searchPlants(filters: SearchPlantFilters): UIO[Vector[Plant]]
  def backupPlants(): UIO[Unit]

object PlantService:
  val live: URLayer[Database & IdGenerator, PlantService] =
    ZLayer.fromZIO {
      for
        db <- ZIO.service[Database]
        idGenerator <- ZIO.service[IdGenerator]
      yield LivePlantService(LivePlantRepo(idGenerator), db)
    }

class LivePlantService(repo: PlantRepo, db: Database) extends PlantService:
  override def createPlant(
      name: PlantName,
      details: PlantDetails,
  ): UIO[PlantId] =
    db.transactionOrDie(repo.createPlant(name, details)) <* backupPlants()

  override def deletePlant(id: PlantId): UIO[Unit] =
    ZIO.attempt(???).orDie

  override def getPlant(id: PlantId): UIO[Option[Plant]] =
    ZIO.attempt(???).orDie

  override def searchPlants(filters: SearchPlantFilters): UIO[Vector[Plant]] =
    db.transactionOrDie(repo.searchPlants(filters))

  override def backupPlants(): UIO[Unit] =
    db.transactionOrDie(repo.searchPlants(SearchPlantFilters.empty))
      .flatMap { plants =>
        ZIO.attemptBlocking {
          Files.writeString(Paths.get("plants.json"), plants.toJson)
        }.orDie
      }
      .unit
