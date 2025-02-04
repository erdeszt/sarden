package org.sarden.core.domain.plant

import com.github.f4b6a3.ulid.Ulid
import doobie.Read

opaque type PlantName = String

object PlantName:
  def apply(raw: String): PlantName = raw

opaque type PlantId = Ulid

object PlantId:
  def apply(raw: Ulid): PlantId = raw

case class PlantDetails(
)

case class Plant(
    id: PlantId,
    name: PlantName,
    details: PlantDetails,
)

case class SearchPlantFilters(
    name: Option[PlantName],
)
