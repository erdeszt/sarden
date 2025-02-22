package org.sarden.web.routes.pages.plants

import io.scalaland.chimney.dsl.*
import scalatags.Text.TypedTag
import scalatags.Text.all.*
import sttp.model.{HeaderNames, StatusCode}
import sttp.tapir.Schema
import sttp.tapir.ztapir.*
import zio.*

import org.sarden.core.mapping.given
import org.sarden.core.plant.*
import org.sarden.web.AppServerEndpoint
import org.sarden.web.routes.pages.*

private[pages] case class CreateCompanionFormBody(
    companionId: String,
    benefits: List[String],
) derives Schema

private[pages] case class CreateCompanionVM(
    targetPlant: PlantVM,
    allPlants: Vector[PlantVM],
) derives Schema

val createCompanionForm: AppServerEndpoint = baseEndpoint.get
  .in("plants" / path[String]("plantId") / "companions" / "new")
  .out(htmlView[CreateCompanionVM](createCompanionView))
  .zServerLogic: rawTargetPlantId =>
    for
      targetPlantId <- ZIO.fromEither {
        rawTargetPlantId
          .transformIntoPartial[PlantId]
          .asEither
          .left
          .map(_ => InvalidPlantIdInputError(rawTargetPlantId))
      }.orDie
      plantService <- ZIO.service[PlantService]
      targetPlant <- plantService.getPlant(targetPlantId).orDie
      allPlants <- plantService.searchPlants(SearchPlantFilters.empty)
    yield CreateCompanionVM(
      targetPlant.transformInto[PlantVM],
      allPlants.map(_.transformInto[PlantVM]),
    )

val createCompanion: AppServerEndpoint = baseEndpoint.post
  .in("plants" / path[String]("plantId") / "companions" / "new")
  .in(formBody[CreateCompanionFormBody])
  .out(
    statusCode(StatusCode.Found)
      .and(sttp.tapir.header[String](HeaderNames.Location)),
  )
  .zServerLogic(_ => ZIO.succeed("/plants"))

private def createCompanionView(vm: CreateCompanionVM): TypedTag[String] =
  layout(
    div(
      cls := "container-fluid",
      h1(s"New companion for: ${vm.targetPlant.name}"),
      form(
        action := s"/plants/${vm.targetPlant.id}/companions/new",
        method := "post",
        div(
          cls := "mb-3",
          label(`for` := "companionId", cls := "form-label", "Companion"),
          select(
            id := "companionId",
            name := "companionId",
            `cls` := "form-select",
            for plant <- vm.allPlants
            yield option(value := plant.id, plant.name),
          ),
        ),
        div(
          cls := "mb-3 form-check",
          input(
            cls := "form-check-input",
            `type` := "checkbox",
            value := "attracts_beneficial_bugs",
            id := "benefitAttractsBeneficialBugs",
            name := "benefitAttractsBeneficialBugs",
          ),
          label(
            `for` := "benefitAttractsBeneficialBugs",
            cls := "form-check-label",
            "Attracts beneficial bugs",
          ),
        ),
        div(
          cls := "mb-3 form-check",
          input(
            cls := "form-check-input",
            `type` := "checkbox",
            value := "attracts_beneficial_bugs",
            id := "benefitAttractsPollinators",
            name := "benefitAttractsPollinators",
          ),
          label(
            `for` := "benefitAttractsPollinators",
            cls := "form-check-label",
            "Attracts pollinators",
          ),
        ),
        div(
          cls := "mb-3 form-check",
          input(
            cls := "form-check-input",
            `type` := "checkbox",
            value := "attracts_beneficial_bugs",
            id := "benefitDetersPests",
            name := "benefitDetersPests",
          ),
          label(
            `for` := "benefitDetersPests",
            cls := "form-check-label",
            "Deters pests",
          ),
        ),
        button(
          `type` := "submit",
          cls := "btn btn-primary",
          "Create",
        ),
      ),
    ),
  )
