package org.sarden.web.routes.api

import sttp.tapir.json.zio.*
import sttp.tapir.ztapir.*
import zio.*

import org.sarden.core.todo.*
import org.sarden.web.*
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

def todoEndpoints: List[AppServerEndpoint] =
  List(
    todosEndpoint.zServerLogic(_ =>
      ZIO.serviceWithZIO[TodoService](_.getActiveTodos()),
    ),
    createTodoEndpoint.zServerLogic(createTodo =>
      ZIO.serviceWithZIO[TodoService](_.createTodo(createTodo)),
    ),
  )
