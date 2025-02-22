package org.sarden.web.routes.pages.plants

import io.scalaland.chimney.Transformer
import io.scalaland.chimney.dsl.*
import scalatags.Text.TypedTag
import scalatags.Text.all.*
import sttp.tapir.Schema
import sttp.tapir.ztapir.*
import zio.*

import org.sarden.core.InvalidRequestError
import org.sarden.core.mapping.given
import org.sarden.core.plant.*
import org.sarden.web.*
import org.sarden.web.routes.pages.*

case class InvalidPlantIdInputError(raw: String)
    extends InvalidRequestError(s"Invalid plant id: ${raw}")

given Transformer[CompanionBenefit, String] =
  case CompanionBenefit.AttractsBeneficialBugs => "Attracts beneficial bugs"
  case CompanionBenefit.AttractsPollinators    => "Attracts pollinators"
  case CompanionBenefit.DetersPests            => "Deters pests"

case class ViewPlantVM(plant: PlantVM, companions: Vector[CompanionVM])
    derives Schema

val viewPlant: AppServerEndpoint = baseEndpoint.get
  .in("plants" / path[String]("id"))
  .out(htmlView[ViewPlantVM](viewPlantView))
  .zServerLogic: rawId =>
    for
      id <- ZIO.fromEither {
        rawId
          .transformIntoPartial[PlantId]
          .asEither
          .left
          .map(_ => InvalidPlantIdInputError(rawId))
      }.orDie
      plantService <- ZIO.service[PlantService]
      plant <- plantService.getPlant(id).orDie
      companions <- plantService
        .getCompanionsOfPlant(plant.id)
        .orDie
    yield ViewPlantVM(
      plant.transformInto[PlantVM],
      companions.map(_.transformInto[CompanionVM]),
    )

private def viewPlantView(vm: ViewPlantVM): TypedTag[String] =
  layout(
    div(
      cls := "container py-5",
      div(
        cls := "row justify-content-center",
        div(
          cls := "col-lg-10",
          div(
            cls := "card border-1 shadow-sm",
            div(
              cls := "row g-0",
              div(
                cls := "col-md-2",
                // TODO: Plant image
                img(
                  src := "/assets/images/carrot.jpg",
                  cls := "img-fluid rounded-start",
                  alt := "Carrot",
                ),
              ),
              div(
                cls := "col-md-10",
                div(
                  cls := "card-body",
                  h5(cls := "card-title", s"${vm.plant.name}"),
                  h6(
                    cls := "card-subtitle mb-2 text-body-secondary",
                    s"${vm.plant.name} in latin...",
                  ),
                  p(cls := "card-text", "Some description of the plant"),
                  a(
                    href := s"#companions-list",
                    cls := "btn btn-success btn-sm",
                    attr("data-bs-toggle") := "collapse",
                    attr("role") := "button",
                    attr("aria-expanded") := "false",
                    attr("aria-controls") := "companions-list",
                    "Companions",
                  ),
                  div(
                    cls := "collapse",
                    id := "companions-list",
                    ul(
                      cls := "list-group",
                      for companion <- vm.companions
                      yield li(
                        cls := "list-group-item",
                        s"${companion.companionPlant.name} - ${companion.benefits.mkString(",")}",
                      ),
                    ),
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    ),
  )
