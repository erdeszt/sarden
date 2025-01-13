package org.sarden.core.domain.plant

opaque type PlantName = String

object PlantName:
  def apply(raw: String): PlantName = raw

opaque type PlantId = String

object PlantId:
  def apply(raw: String): PlantId = raw

case class PlantDetails(
)

case class Plant(
    id: PlantId,
    name: PlantName,
    details: PlantDetails,
)

case class SearchPlantFilters(
    name: Option[PlantName],
    details: PlantDetails,
)
