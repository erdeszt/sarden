package org.sarden.core.plant

import scala.util.Try

import com.github.f4b6a3.ulid.Ulid
import io.scalaland.chimney.*
import io.scalaland.chimney.partial.Result
import neotype.*

type PlantName = PlantName.Type
object PlantName extends Newtype[String]:
  override inline def validate(input: String): Boolean =
    input.nonEmpty

// TODO: Move this to common place
trait UlidNewtype extends Newtype[Ulid] {
  self =>
  given PartialTransformer[String, self.Type] = PartialTransformer(raw =>
    Result.fromEitherString(
      Try(Ulid.from(raw)).toEither.left
        .map(_.getMessage)
        // NOTE: This is safe because self.Type =:= Ulid
        .map(_.asInstanceOf[self.Type]),
    ),
  )
}

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
