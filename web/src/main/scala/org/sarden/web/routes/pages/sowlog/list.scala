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
    ul(
      for entry <- sowlogEntries
      yield li(s"${entry.id}: ${entry.plant.name} - ${entry.sowingDate}"),
    ),
  )
