package org.sarden.core.domain.plant

import zio.*

import org.sarden.core.domain.plant.internal.PlantRepo

trait PlantService:
  def createPlant(name: PlantName, details: PlantDetails): UIO[PlantId]
  def deletePlant(id: PlantId): UIO[Unit]
  def getPlant(id: PlantId): UIO[Option[Plant]]
  def searchPlants(filters: SearchPlantFilters): UIO[Vector[Plant]]

class LivePlantService(repo: PlantRepo) extends PlantService:
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
    repo.searchPlants(filters)
