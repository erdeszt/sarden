package org.sarden.core.domain.plant

import com.github.f4b6a3.ulid.Ulid
import doobie.{Get, Read}

opaque type PlantName = String

object PlantName:
  def apply(raw: String): PlantName = raw
  given get: Get[PlantName] = Get[String].map(raw => raw)

opaque type PlantId = Ulid

object PlantId:
  def apply(raw: Ulid): PlantId = raw
  given get: Get[PlantId] = Get[String].map(raw => Ulid.from(raw))

case class PlantDetails(
)

case class Plant(
    id: PlantId,
    name: PlantName,
) derives Read

case class SearchPlantFilters(
    name: Option[PlantName],
)
