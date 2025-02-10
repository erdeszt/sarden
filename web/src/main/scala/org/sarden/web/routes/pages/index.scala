package org.sarden.web.routes.pages

import scalatags.Text.TypedTag
import scalatags.Text.all.*
import sttp.tapir.ztapir.*
import zio.*

import org.sarden.web.*

val index: AppServerEndpoint = baseEndpoint.get
  .out(htmlView[Unit](indexView))
  .zServerLogic(_ => ZIO.unit)

private def indexView(_dummy: Unit): TypedTag[String] =
  layout(
    tag("main")(
      cls := "container-fluid",
      h1("HELLO"),
    ),
  )
