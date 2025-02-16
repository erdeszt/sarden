package org.sarden.web.routes.pages.sowlog

import java.time.LocalDate

import io.scalaland.chimney.dsl.*
import scalatags.Text.TypedTag
import scalatags.Text.all.*
import sttp.model.{HeaderNames, StatusCode}
import sttp.tapir.Schema
import sttp.tapir.ztapir.*
import zio.*
import zio.json.*

import org.sarden.core.SystemErrors.{DataFormatError, DataInconsistencyError}
import org.sarden.core.mapping.given
import org.sarden.core.plant.PlantId
import org.sarden.core.sowlog.*
import org.sarden.web.*
import org.sarden.web.routes.pages.*

private[pages] case class CreateSowlogEntryForm(
    plantId: String,
    sowingDate: String,
) derives Schema

val createSowlogEntryForm: AppServerEndpoint = baseEndpoint.get
  .in("sowlog" / "new")
  .out(htmlView[Unit](createView))
  .zServerLogic(_ => ZIO.unit)

val createSowlogEntry: AppServerEndpoint = baseEndpoint.post
  .in("sowlog" / "new")
  .in(formBody[CreateSowlogEntryForm])
  .out(
    statusCode(StatusCode.Found)
      .and(sttp.tapir.header[String](HeaderNames.Location)),
  )
  .zServerLogic { formData =>
    for
      sowingDate <- ZIO
        .fromEither {
          s"\"${formData.sowingDate}\"".fromJson[LocalDate]
        }
        .mapError(DataFormatError(_))
        .orDie
      plantId <- ZIO.fromEither {
        formData.plantId
          .transformIntoPartial[PlantId]
          .asEither
          .left
          .map(DataInconsistencyError(_))
      }.orDie
      _ <- ZIO.serviceWithZIO[SowlogService]:
        _.createEntry(plantId, sowingDate, SowlogDetails())
    yield "/sowlog"
  }

private def createView(_unit: Unit): TypedTag[String] =
  layout(
    div(
      cls := "container-fluid",
      h1("Add sow log entry"),
      form(
        action := "/sowlog/new",
        method := "post",
        div(
          cls := "mb-3",
          label(`for` := "plantId", cls := "form-label", "Plant"),
          // TODO: Dropdown
          input(`type` := "text", id := "plantId", name := "plantId"),
        ),
        div(
          cls := "mb-3",
          label(`for` := "sowingDate", cls := "form-label", "Sowing Date"),
          // TODO: Date picker
          input(`type` := "text", id := "sowingDate", name := "sowingDate"),
        ),
        button(
          `type` := "submit",
          cls := "btn btn-primary",
          "Add",
        ),
      ),
    ),
  )
