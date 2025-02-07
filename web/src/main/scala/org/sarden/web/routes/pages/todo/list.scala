package org.sarden.web.routes.pages.todo

import scalatags.Text.TypedTag
import scalatags.Text.all.*
import sttp.tapir.ztapir.*
import zio.*
import zio.json.*

import org.sarden.core.todo.*
import org.sarden.web.AppServerEndpoint
import org.sarden.web.routes.pages.*
import org.sarden.web.routes.schemas.todo.given

val listTodos: AppServerEndpoint = baseEndpoint.get
  .in("todos")
  .out(htmlView[Vector[Todo]](listView))
  .zServerLogic { (_: Unit) =>
    ZIO.serviceWithZIO[TodoService](_.getActiveTodos())
  }

private def listView(todos: Vector[Todo]): TypedTag[String] =
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
              th(attr("scope") := "row", todo.id.unwrap.toString),
              td(todo.name.unwrap),
              td(todo.schedule.toJson),
              td(s"${todo.notifyBefore.toHours} Hours"),
              td(todo.lastRun.map(_.toString).getOrElse("n. / a.")),
              td(
                form(
                  action := s"/todos/delete/${todo.id.unwrap}",
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
