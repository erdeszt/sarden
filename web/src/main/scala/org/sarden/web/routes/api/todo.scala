package org.sarden.web.routes.api

import com.github.f4b6a3.ulid.Ulid
import sttp.tapir.json.zio.*
import sttp.tapir.ztapir.*
import zio.*

import org.sarden.core.domain.todo.*
import org.sarden.web.*
import org.sarden.web.routes.pages.htmlView
import org.sarden.web.routes.schemas.todo.given

val todosEndpoint = baseEndpoint.get
  .in("todos")
  .out(
    oneOfBody(
      jsonBody[Vector[Todo]],
    ),
  )

val createTodoEndpoint = baseEndpoint.post
  .in("todos")
  .in(jsonBody[CreateTodo])
  .out(jsonBody[Todo])

// TODO: Remove this view when the htmx delete logic is removed
val deleteTodoEndpoint = baseEndpoint.delete
  .in("todos" / path[String]("id"))
  .out(htmlView[Unit](_ => scalatags.Text.all.div()))

def todoEndpoints: List[AppServerEndpoint] =
  List(
    todosEndpoint.zServerLogic(_ =>
      ZIO.serviceWithZIO[TodoService](_.getActiveTodos()),
    ),
    createTodoEndpoint.zServerLogic(createTodo =>
      ZIO.serviceWithZIO[TodoService](_.createTodo(createTodo)),
    ),
    deleteTodoEndpoint.zServerLogic(id =>
      ZIO.serviceWithZIO[TodoService](_.deleteTodo(TodoId(Ulid.from(id)))),
    ),
  )
