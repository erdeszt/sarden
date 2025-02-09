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

private def listView(todos: Vector[TodoVM]): TypedTag[String] =
  layout(
    div(
      cls := "container-fluid",
      table(
        cls := "table table-striped",
        thead(
          tr(
            th("ID"),
            th("Name"),
            th("Schedule"),
            th("Notify before"),
            th("Last run"),
            th("Actions"),
          ),
        ),
        tbody(
          for (todo <- todos)
            yield tr(
              th(attr("scope") := "row", todo.id),
              td(todo.name),
              td(todo.schedule),
              td(s"${todo.notifyBefore.toHours} Hours"),
              td(todo.lastRun.map(_.toString).getOrElse("n. / a.")),
              td(
                form(
                  action := s"/todos/delete/${todo.id}",
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
      div(
        form(
          attr("hx-post") := "/todos",
        ),
      ),
    ),
  )
