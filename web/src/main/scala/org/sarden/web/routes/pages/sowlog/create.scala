package org.sarden.web.routes.pages.sowlog

import java.time.LocalDate

import io.scalaland.chimney.dsl.*
import scalatags.Text.TypedTag
import scalatags.Text.all.*
import sttp.model.{HeaderNames, StatusCode}
import sttp.tapir.Schema
import sttp.tapir.ztapir.*
import zio.*

import org.sarden.core.InvalidRequestError
import org.sarden.core.mapping.given
import org.sarden.core.plant.*
import org.sarden.core.sowlog.*
import org.sarden.web.*
import org.sarden.web.routes.pages.*
import org.sarden.web.routes.pages.plants.{InvalidPlantIdInputError, PlantVM}

private[pages] case class CreateSowlogEntryForm(
    plantId: String,
    sowingDateYear: Int,
    sowingDateMonth: Int,
    sowingDateDay: Int,
) derives Schema

case class InvalidLocalDateInputError(year: Int, month: Int, day: Int)
    extends InvalidRequestError(
      s"Invalid LocalDate parts input, year=${year}, month=${month}, day=${day}",
    )

val createSowlogEntryForm: AppServerEndpoint = baseEndpoint.get
  .in("sowlog" / "new")
  .out(htmlView[Vector[PlantVM]](createView))
  .zServerLogic: _ =>
    ZIO.serviceWithZIO[PlantService]: plantService =>
      plantService
        .searchPlants(SearchPlantFilters(None))
        .map(_.map(_.transformInto[PlantVM]))

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
        .attempt {
          LocalDate.of(
            formData.sowingDateYear,
            formData.sowingDateMonth,
            formData.sowingDateDay,
          )
        }
        .orElseFail(
          InvalidLocalDateInputError(
            formData.sowingDateYear,
            formData.sowingDateMonth,
            formData.sowingDateDay,
          ),
        )
        .orDie
      plantId <- ZIO
        .fromEither(PlantId.fromString(formData.plantId))
        .orElseFail(InvalidPlantIdInputError(formData.plantId))
        .orDie
      _ <- ZIO.serviceWithZIO[SowlogService]:
        _.createEntry(plantId, sowingDate, SowlogDetails())
    yield "/sowlog"
  }

private def createView(plants: Vector[PlantVM]): TypedTag[String] =
  layout(
    div(
      cls := "container-fluid",
      h1("Add sow log entry"),
      form(
        action := "/sowlog/new",
        method := "post",
        div(
          cls := "input-group",
          span(cls := "input-group-text", "Plant"),
          // TODO: Dropdown
          select(
            cls := "form-select",
            name := "plantId",
            for plant <- plants yield option(value := plant.id, plant.name),
          ),
        ),
        div(
          cls := "input-group",
          span(cls := "input-group-text", "Sowing date"),
          // TODO: Date picker
          input(
            `type` := "number",
            cls := "form-control",
            id := "sowingDateYear",
            name := "sowingDateYear",
            required,
            placeholder := "Year",
          ),
          input(
            `type` := "number",
            cls := "form-control",
            id := "sowingDateMonth",
            name := "sowingDateMonth",
            required,
            placeholder := "Month",
          ),
          input(
            `type` := "number",
            cls := "form-control",
            id := "sowingDateDay",
            name := "sowingDateDay",
            required,
            placeholder := "Day",
          ),
        ),
        button(
          `type` := "submit",
          cls := "btn btn-primary",
          "Add",
        ),
      ),
    ),
  )
