package org.sarden.web.routes.pages.plants

import io.scalaland.chimney.dsl.*
import scalatags.Text.TypedTag
import scalatags.Text.all.*
import sttp.tapir.Schema
import sttp.tapir.ztapir.*
import zio.*

import org.sarden.core.mapping.given
import org.sarden.core.plant.*
import org.sarden.web.AppServerEndpoint
import org.sarden.web.routes.pages.*

private[routes] case class PlantVM(
    id: String,
    name: String,
) derives Schema

val listPlants: AppServerEndpoint = baseEndpoint.get
  .in("plants")
  .out(htmlView[Vector[PlantVM]](listView))
  .zServerLogic { (_: Unit) =>
    ZIO.serviceWithZIO[PlantService]:
      _.searchPlants(SearchPlantFilters(None))
        .map(_.map(_.transformInto[PlantVM]))
  }

private def listView(plants: Vector[PlantVM]): TypedTag[String] =
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
              cls := "card-body p-4",
              // Header:
              div(
                cls := "d-flex justify-content-between align-items-center mb-4",
                h4(cls := "mb-0", "Plants"),
                div(
                  cls := "d-flex gap-2",
                  button(cls := "btn btn-outline-secondary btn-sm", "Export"),
                  button(cls := "btn btn-primary btn-sm", "Filter"),
                ),
              ),
              // Search:
              div(
                cls := "row g-3 mb-4",
                div(
                  cls := "col-md-6",
                  input(
                    cls := "form-control",
                    `type` := "text",
                    placeholder := "Search plants...",
                  ),
                ),
                div(
                  cls := "col-md-6",
                  div(
                    cls := "d-flex gap-2 flex-wrap",
                    span(cls := "badge px-3 py-2 text-bg-primary", "All"),
                    span(
                      cls := "badge px-3 py-2 text-bg-secondary",
                      "Summer",
                    ),
                    span(cls := "badge px-3 py-2 text-bg-secondary", "Winter"),
                  ),
                ),
              ),
              // Search results:
              div(
                cls := "row g-3 mb-4",
                for plant <- plants
                yield div(
                  cls := "card border-info mb-3",
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
                          href := s"/plants/${plant.id}",
                          cls := "btn btn-primary btn-sm",
                          "Details",
                        ),
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
        ),
      ),
    ),
  )
