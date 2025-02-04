package org.sarden.core.domain.plant

import io.github.gaelrenoux.tranzactio.*
import io.github.gaelrenoux.tranzactio.doobie.*
import zio.*

import org.sarden.core.domain.plant.internal.*

trait PlantService:
  def createPlant(name: PlantName, details: PlantDetails): UIO[PlantId]
  def deletePlant(id: PlantId): UIO[Unit]
  def getPlant(id: PlantId): UIO[Option[Plant]]
  def searchPlants(filters: SearchPlantFilters): UIO[Vector[Plant]]

object PlantService:
  val live: URLayer[Database, PlantService] =
    ZLayer.fromFunction(LivePlantService(LivePlantRepo(), _))

class LivePlantService(repo: PlantRepo, db: Database) extends PlantService:
  override def createPlant(
      name: PlantName,
      details: PlantDetails,
  ): UIO[PlantId] =
    ZIO.attempt(???).orDie

  override def deletePlant(id: PlantId): UIO[Unit] =
    ZIO.attempt(???).orDie

  override def getPlant(id: PlantId): UIO[Option[Plant]] =
    ZIO.attempt(???).orDie

  override def searchPlants(filters: SearchPlantFilters): UIO[Vector[Plant]] =
    db.transactionOrDie(repo.searchPlants(filters))
