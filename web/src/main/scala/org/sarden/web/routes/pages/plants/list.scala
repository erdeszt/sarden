package org.sarden.web.routes.pages.plants

import scalatags.Text.TypedTag
import scalatags.Text.all.*
import sttp.tapir.ztapir.*
import zio.*

import org.sarden.core.plant.*
import org.sarden.web.AppServerEndpoint
import org.sarden.web.routes.pages.*
import org.sarden.web.routes.schemas.plants.given

val listPlants: AppServerEndpoint = baseEndpoint.get
  .in("plants")
  .out(htmlView[Vector[Plant]](listView))
  .zServerLogic { (_: Unit) =>
    ZIO.serviceWithZIO[PlantService](_.searchPlants(SearchPlantFilters(None)))
  }

private def listView(plants: Vector[Plant]): TypedTag[String] =
  layout(
    div(
      cls := "container-fluid",
      div(
        a(href := "/plants/new", cls := "btn btn-primary", "Add new plant"),
      ),
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
              td(plant.name.unwrap),
              td(
                a(
                  href := s"/plants/edit/${plant.id.unwrap.toString}",
                  cls := "btn btn-warning",
                  "Edit",
                ),
                form(
                  action := s"/plants/delete/${plant.id.unwrap.toString}",
                  method := "post",
                  button(
                    `type` := "submit",
                    cls := "btn btn-danger",
                    "Delete",
                  ),
                ),
              ),
            ),
        ),
      ),
    ),
  )
