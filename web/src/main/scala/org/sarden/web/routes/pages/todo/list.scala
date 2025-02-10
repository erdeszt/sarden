package org.sarden.web.routes.pages.todo

import io.scalaland.chimney.dsl.*
import scalatags.Text.TypedTag
import scalatags.Text.all.*
import sttp.tapir.Schema
import sttp.tapir.ztapir.*
import zio.*
import zio.json.*

import org.sarden.core.mapping.{*, given}
import org.sarden.core.time.*
import org.sarden.core.todo.*
import org.sarden.web.AppServerEndpoint
import org.sarden.web.routes.pages.*

// TODO: Move to common place and define better
given Schema[FiniteDuration] = Schema.anyObject

given JsonCodec[TodoSchedule] = JsonCodec.derived

given PartialTransformer[String, TodoSchedule] = PartialTransformer: raw =>
  Result.fromEitherString(raw.fromJson)

given Transformer[TodoSchedule, String] = _.toJson

private[pages] case class TodoVM(
    id: String,
    name: String,
    schedule: String,
    notifyBefore: FiniteDuration,
    lastRun: Option[Long],
) derives Schema

val listTodos: AppServerEndpoint = baseEndpoint.get
  .in("todos")
  .out(htmlView[Vector[TodoVM]](listView))
  .zServerLogic { (_: Unit) =>
    ZIO.serviceWithZIO[TodoService]:
      _.getActiveTodos().map(_.map(_.transformInto[TodoVM]))
  }

// TODO: Generalize list page code
private def listView(todos: Vector[TodoVM]): TypedTag[String] =
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
                h4(cls := "mb-0", "TODOs"),
              ),
              // Add todo:
              div(
                cls := "row g-3 mb-4",
                div(
                  cls := "col-md-6",
                  input(
                    cls := "form-control",
                    `type` := "text",
                    placeholder := "Add todo...",
                  ),
                ),
                div(
                  cls := "col-md-6",
                  div(
                    cls := "d-flex gap-2 flex-wrap",
                    span(
                      cls := "badge px-3 py-2 text-bg-primary",
                      "Important",
                    ),
                    span(
                      cls := "badge px-3 py-2 text-bg-secondary",
                      "Not important",
                    ),
                  ),
                ),
              ),
              // Todos:
              div(
                cls := "row g-3 mb-4",
                for todo <- todos
                yield div(
                  cls := "card border-info mb-3",
                  div(
                    cls := "row g-0",
                    div(
                      cls := "card-body",
                      h5(cls := "card-title", s"${todo.name}"),
                      h6(
                        cls := "card-subtitle mb-2 text-body-secondary",
                        s"${todo.schedule}",
                      ),
                      a(
                        href := s"/todos/${todo.id}/complete",
                        cls := "btn btn-primary",
                        "Complete",
                      ),
                      a(
                        // TODO: Use form post
                        href := s"/todos/${todo.id}/delete",
                        cls := "btn btn-danger",
                        "Delete",
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
