package org.sarden.domain.plant

import cats.Order
import neotype.*

import org.sarden.bindings.ulid.*

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
object VarietyName extends Newtype[String]:
  given CanEqual[Type, Type] = CanEqual.derived

case class Variety[PlantType](
    id: VarietyId,
    plant: PlantType,
    name: VarietyName,
)

case class SearchPlantFilters(
    name: Option[PlantName],
)

object SearchPlantFilters:
  def empty: SearchPlantFilters = SearchPlantFilters(None)

type CompanionId = CompanionId.Type
object CompanionId extends UlidNewtype

enum CompanionBenefit derives CanEqual:
  case AttractsBeneficialBugs
  case AttractsPollinators
  case DetersPests

object CompanionBenefit:
  given Ordering[CompanionBenefit] = (a, b) => a.toString.compareTo(b.toString)
  given Order[CompanionBenefit] = Order.fromOrdering

case class Companion[PlantType](
    id: CompanionId,
    companionPlant: PlantType,
    targetPlant: PlantType,
    benefits: Set[CompanionBenefit],
)
