package org.sarden.domain.plant.internal

import cats.syntax.all.*
import doobie.{Get, Put, Read}
import io.scalaland.chimney.partial.Result
import io.scalaland.chimney.{PartialTransformer, Transformer}
import org.sarden.domain.plant.CompanionBenefit
import zio.json.*

private[internal] case class PlantDTO(
    id: String,
    name: String,
) derives Read

private[internal] case class CreatePlantDTO(
    name: String,
) derives JsonCodec

private[internal] case class VarietyDTO(
    id: String,
    plantId: String,
    name: String,
) derives Read

object BenefitMapping:

  private val AttractsBeneficialBugsStringFormat = "attracts_beneficial_bugs"
  private val AttractsPollinatorsStringFormat = "attracts_pollinators"
  private val PestControlStringFormat = "deters_pests"

  def fromStorageFormat
      : String => Either[InvalidCompanionBenefitFormatError, CompanionBenefit] =
    case `AttractsBeneficialBugsStringFormat` =>
      Right(CompanionBenefit.AttractsBeneficialBugs)
    case `AttractsPollinatorsStringFormat` =>
      Right(CompanionBenefit.AttractsPollinators)
    case `PestControlStringFormat` =>
      Right(CompanionBenefit.DetersPests)
    case raw => Left(InvalidCompanionBenefitFormatError(raw))

  def toStorageFormat: CompanionBenefit => String =
    case CompanionBenefit.AttractsBeneficialBugs =>
      AttractsBeneficialBugsStringFormat
    case CompanionBenefit.AttractsPollinators =>
      AttractsPollinatorsStringFormat
    case CompanionBenefit.DetersPests =>
      PestControlStringFormat

case class BenefitsDTO(benefits: Set[String])

object BenefitsDTO:
  def fromBenefits(benefits: Set[CompanionBenefit]): BenefitsDTO =
    new BenefitsDTO(benefits.map(BenefitMapping.toStorageFormat))

given Get[BenefitsDTO] = Get[String].map: raw =>
  raw.fromJson[Set[String]] match
    case Left(error)        => throw InvalidCompanionBenefitFormatError(raw)
    case Right(rawBenefits) => BenefitsDTO(rawBenefits)

given Put[BenefitsDTO] = Put[String].contramap: dto =>
  dto.benefits.toJson

case class CompanionDTO(
    id: String,
    companionPlantId: String,
    targetPlantId: String,
    benefits: BenefitsDTO,
) derives Read

given PartialTransformer[String, CompanionBenefit] = PartialTransformer: raw =>
  Result.fromEitherString(
    BenefitMapping.fromStorageFormat(raw).left.map(_.getMessage),
  )

given Transformer[CompanionBenefit, String] = BenefitMapping.toStorageFormat(_)
