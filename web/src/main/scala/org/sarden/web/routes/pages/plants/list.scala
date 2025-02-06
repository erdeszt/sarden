package org.sarden.web.routes.pages.plants

import scalatags.Text.TypedTag
import scalatags.Text.all.*
import sttp.tapir.ztapir.*
import zio.*

import org.sarden.core.domain.plant.*
import org.sarden.web.AppServerEndpoint
import org.sarden.web.routes.pages.*
import org.sarden.web.routes.schemas.plants.given

val listPlants: AppServerEndpoint = baseEndpoint.get
  .in("plants")
  .out(htmlView[Vector[Plant]](view))
  .zServerLogic { (_: Unit) =>
    ZIO.serviceWithZIO[PlantService](_.searchPlants(SearchPlantFilters(None)))
  }

private def view(plants: Vector[Plant]): TypedTag[String] =
  layout(
    div(
      cls := "container-fluid",
      table(
        cls := "table table-striped",
        thead(
          tr(
            th("ID"),
            th("Name"),
            th("Actions"),
          ),
        ),
        tbody(
          for (plant <- plants)
            yield tr(
              th(attr("scope") := "row", plant.id.unwrap.toString),
              th(plant.name.unwrap),
            ),
        ),
      ),
    ),
  )
