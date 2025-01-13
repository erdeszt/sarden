package org.sarden.core.domain.plant

trait PlantService:
  def createPlant(name: PlantName, details: PlantDetails): PlantId
  def deletePlant(id: PlantId): Unit
  def getPlant(id: PlantId): Option[Plant]
  def searchPlants(filters: SearchPlantFilters): List[Plant]

class LivePlantService() extends PlantService:
  override def createPlant(name: PlantName, details: PlantDetails): PlantId =
    ???

  override def deletePlant(id: PlantId): Unit =
    ???

  override def getPlant(id: PlantId): Option[Plant] =
    ???

  override def searchPlants(filters: SearchPlantFilters): List[Plant] =
    ???
