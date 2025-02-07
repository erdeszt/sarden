package org.sarden.core.plant

import com.github.f4b6a3.ulid.Ulid
import neotype.*

import org.sarden.core.UlidNewtype

type PlantName = PlantName.Type
object PlantName extends Newtype[String]:
  override inline def validate(input: String): Boolean =
    input.nonEmpty

type PlantId = PlantId.Type
object PlantId extends UlidNewtype

case class PlantDetails(
)

case class Plant(
    id: PlantId,
    name: PlantName,
)

case class SearchPlantFilters(
    name: Option[PlantName],
)

object SearchPlantFilters:
  def empty: SearchPlantFilters = SearchPlantFilters(None)
