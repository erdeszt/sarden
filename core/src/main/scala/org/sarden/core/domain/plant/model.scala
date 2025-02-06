package org.sarden.core.domain.plant

import com.github.f4b6a3.ulid.Ulid
import doobie.{Get, Read}
import zio.json.*

opaque type PlantName = String

extension (name: PlantName) def unwrap: String = name

object PlantName:
  def apply(raw: String): PlantName = raw
  given Get[PlantName] = Get[String].map(raw => raw)
  given JsonDecoder[PlantName] = JsonDecoder[String].map(raw => raw)
  given JsonEncoder[PlantName] = JsonEncoder[String].contramap(raw => raw)

opaque type PlantId = Ulid

extension (id: PlantId) def unwrap: Ulid = id

object PlantId:
  def apply(raw: Ulid): PlantId = raw
  given Get[PlantId] = Get[String].map(raw => Ulid.from(raw))
  given JsonDecoder[PlantId] =
    JsonDecoder[String].map(raw => Ulid.from(raw))
  given JsonEncoder[PlantId] = JsonEncoder[String].contramap(id => id.toString)

case class PlantDetails(
)

case class Plant(
    id: PlantId,
    name: PlantName,
) derives Read,
      JsonCodec

case class SearchPlantFilters(
    name: Option[PlantName],
)

object SearchPlantFilters:
  def empty: SearchPlantFilters = SearchPlantFilters(None)
