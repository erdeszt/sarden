package org.sarden.core.plant

import neotype.*

import org.sarden.core.ulid.*

type PlantName = PlantName.Type
object PlantName extends Newtype[String]:
  given CanEqual[PlantName, PlantName] = CanEqual.derived

type PlantId = PlantId.Type
object PlantId extends UlidNewtype

case class PlantDetails(
)

case class Plant(
    id: PlantId,
    name: PlantName,
)

type VarietyId = VarietyId.Type
object VarietyId extends UlidNewtype

type VarietyName = VarietyName.Type
object VarietyName extends Newtype[String]

case class Variety(
    id: VarietyId,
    plantId: PlantId,
    name: VarietyName,
)

case class SearchPlantFilters(
    name: Option[PlantName],
)

object SearchPlantFilters:
  def empty: SearchPlantFilters = SearchPlantFilters(None)
