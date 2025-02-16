package org.sarden.web.routes.pages.plants

import io.scalaland.chimney.dsl.*
import scalatags.Text.TypedTag
import scalatags.Text.all.*
import sttp.tapir.ztapir.*
import zio.*

import org.sarden.core.SystemErrors.DataInconsistencyError
import org.sarden.core.mapping.given
import org.sarden.core.plant.*
import org.sarden.web.*
import org.sarden.web.routes.pages.*

val viewPlant: AppServerEndpoint = baseEndpoint.get
  .in("plants" / path[String]("id"))
  .out(htmlView[PlantVM](viewPlantView))
  .zServerLogic: rawId =>
    for
      id <- ZIO.fromEither {
        rawId
          .transformIntoPartial[PlantId]
          .asEither
          .left
          .map(DataInconsistencyError(_))
      }.orDie
      plant <- ZIO
        .serviceWithZIO[PlantService] {
          _.getPlant(id).someOrFail(
            DataInconsistencyError(s"Missing plant with id: ${id}"),
          )
        }
        .orDie
    yield plant.transformInto[PlantVM]

private def viewPlantView(plant: PlantVM): TypedTag[String] =
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
                  h5(cls := "card-title", s"${plant.name}"),
                  h6(
                    cls := "card-subtitle mb-2 text-body-secondary",
                    s"${plant.name} in latin...",
                  ),
                  p(cls := "card-text", "Some description of the plant"),
                  a(
                    href := s"/plants/${plant.id}/companions",
                    cls := "btn btn-success btn-sm",
                    "Companions",
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    ),
  )
