package org.sarden.web.routes.pages.sowlog

import io.scalaland.chimney.dsl.*
import scalatags.Text.TypedTag
import scalatags.Text.all.*
import sttp.tapir.Schema
import sttp.tapir.ztapir.*
import zio.*

import org.sarden.core.mapping.given
import org.sarden.core.sowlog.*
import org.sarden.web.*
import org.sarden.web.routes.pages.*
import org.sarden.web.routes.pages.plants.PlantVM

given Schema[SowlogDetails] = Schema.anyObject

private[routes] case class SowlogEntryVM(
    id: String,
    plant: PlantVM,
    sowingDate: String,
    details: SowlogDetails,
) derives Schema

val showSowlog: AppServerEndpoint = baseEndpoint.get
  .in("sowlog")
  .out(htmlView[Vector[SowlogEntryVM]](listView))
  .zServerLogic { (_: Unit) =>
    ZIO.serviceWithZIO[SowlogService]:
      _.getEntries()
        .map(_.map(_.transformInto[SowlogEntryVM]))
  }

private def listView(sowlogEntries: Vector[SowlogEntryVM]): TypedTag[String] =
  layout(
    div(
      cls := "container-fluid",
      a(
        href := "/sowlog/new",
        cls := "btn btn-primary btn-sm",
        "Add new entry",
      ),
      table(
        cls := "table table-striped",
        thead(
          tr(
            th("Plant"),
            th("Sowing Date"),
            th("Details"),
            th("Actions"),
          ),
        ),
        tbody(
          for entry <- sowlogEntries
          yield tr(
            td(entry.plant.name),
            td(entry.sowingDate),
            td("coming soon..."),
            td(
              form(
                action := s"/sowlog/delete/${entry.id}",
                method := "post",
                button(
                  `type` := "submit",
                  cls := "btn btn-danger btn-sm",
                  "Delete",
                ),
              ),
            ),
          ),
        ),
      ),
    ),
  )
