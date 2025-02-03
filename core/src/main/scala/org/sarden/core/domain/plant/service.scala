package org.sarden.core.domain.plant

import org.sarden.core.domain.plant.internal.PlantRepo

trait PlantService:
  def createPlant(name: PlantName, details: PlantDetails): PlantId
  def deletePlant(id: PlantId): Unit
  def getPlant(id: PlantId): Option[Plant]
  def searchPlants(filters: SearchPlantFilters): Vector[Plant]

class LivePlantService(repo: PlantRepo) extends PlantService:
  override def createPlant(name: PlantName, details: PlantDetails): PlantId =
    ???

  override def deletePlant(id: PlantId): Unit =
    ???

  override def getPlant(id: PlantId): Option[Plant] =
    ???

  override def searchPlants(filters: SearchPlantFilters): Vector[Plant] =
    repo.searchPlants(filters)
