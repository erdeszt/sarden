package org.sarden.web.routes.pages.plants

import io.scalaland.chimney.dsl.*
import neotype.interop.chimney.given
import scalatags.Text.TypedTag
import scalatags.Text.all.*
import sttp.tapir.Schema
import sttp.tapir.ztapir.*
import zio.*

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
              th(attr("scope") := "row", plant.id),
              td(plant.name),
              td(
                a(
                  href := s"/plants/edit/${plant.id}",
                  cls := "btn btn-warning",
                  "Edit",
                ),
                form(
                  action := s"/plants/delete/${plant.id}",
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
