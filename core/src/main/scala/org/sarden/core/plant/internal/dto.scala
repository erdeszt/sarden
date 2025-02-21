package org.sarden.core.plant.internal

import doobie.{Get, Put, Read}
import io.scalaland.chimney.Transformer
import zio.json.*

import org.sarden.core.plant.CompanionBenefit

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

case class BenefitsDTO(benefits: Set[CompanionBenefit])

given Get[BenefitsDTO] = Get[String].map: raw =>
  raw.fromJson[Set[String]] match
    case Left(error) => throw InvalidCompanionBenefitFormatError(raw)
    case Right(rawBenefits) =>
      BenefitsDTO:
        rawBenefits.map: rawBenefit =>
          BenefitMapping.fromStorageFormat(rawBenefit) match
            case Left(error)    => throw InvalidCompanionBenefitFormatError(raw)
            case Right(benefit) => benefit

given Put[BenefitsDTO] = Put[String].contramap: dto =>
  dto.benefits.map(BenefitMapping.toStorageFormat).toJson

case class CompanionDTO(
    id: String,
    companionPlantId: String,
    targetPlantId: String,
    benefits: BenefitsDTO,
) derives Read

given Transformer[BenefitsDTO, Set[CompanionBenefit]] = _.benefits
