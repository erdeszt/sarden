package org.sarden.web.views

import scalatags.Text.TypedTag
import scalatags.Text.all.*

import org.sarden.core.domain.todo.*

def todoList(todos: List[Todo]): TypedTag[String] =
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
          ),
        ),
        tbody(
          for (todo <- todos)
            yield tr(
              th(attr("scope") := "row", todo.id.unwrap.toString),
              td(todo.name.unwrap),
              td(upickle.default.write(todo.schedule)),
              td(s"${todo.notifyBefore.toHours} Hours"),
              td(todo.lastRun.map(_.toString).getOrElse("n. / a.")),
            ),
        ),
      ),
    ),
  )
