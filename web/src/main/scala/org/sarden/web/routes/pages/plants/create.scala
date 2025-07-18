package org.sarden.web.routes.pages.plants

import io.scalaland.chimney.dsl.*
import scalatags.Text.TypedTag
import scalatags.Text.all.*
import sttp.model.{HeaderNames, StatusCode}
import sttp.tapir.Schema
import sttp.tapir.ztapir.*
import zio.*

import org.sarden.domain.plant.*
import org.sarden.bindings.mapping.given
import org.sarden.web.AppServerEndpoint
import org.sarden.web.routes.pages.*

case class CreatePlantForm(
    name: String,
)

given Schema[CreatePlantForm] = Schema.derived

val createPlantForm: AppServerEndpoint = baseEndpoint.get
  .in("plants" / "new")
  .out(htmlView[Unit](createPlantView))
  .zServerLogic(_ => ZIO.unit)

val createPlant: AppServerEndpoint = baseEndpoint.post
  .in("plants" / "new")
  .in(formBody[CreatePlantForm])
  .out(
    statusCode(StatusCode.Found)
      .and(sttp.tapir.header[String](HeaderNames.Location)),
  )
  .zServerLogic { formData =>
    ZIO.serviceWithZIO[PlantService]:
      // TODO: Make safely
      _.createPlant(
        formData.name.transformInto[PlantName],
        PlantDetails(),
      )
        .as("/plants")
  }

private def createPlantView(_unit: Unit): TypedTag[String] =
  layout(
    div(
      cls := "container-fluid",
      h1("New plant"),
      form(
        action := "/plants/new",
        method := "post",
        div(
          cls := "mb-3",
          label(`for` := "name", cls := "form-label", "Name"),
          input(`type` := "text", id := "name", name := "name"),
        ),
        button(
          `type` := "submit",
          cls := "btn btn-primary",
          "Create",
        ),
      ),
    ),
  )
