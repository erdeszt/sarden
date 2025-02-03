package org.sarden.web.endpoints

import scala.concurrent.duration.FiniteDuration

import com.github.f4b6a3.ulid.Ulid
import sttp.shared.Identity
import sttp.tapir.*
import sttp.tapir.json.upickle.*
import sttp.tapir.server.ServerEndpoint

import org.sarden.core.domain.todo.*
import org.sarden.web.*

given Schema[Schedule] = Schema.derived
given Schema[FiniteDuration] = Schema.anyObject
given Schema[TodoId] = Schema.string
given Schema[TodoName] = Schema.string
given Schema[CreateTodo] = Schema.derived
given Schema[Todo] = Schema.derived

val todosEndpoint = endpoint.get
  .in("todos")
  .out(
    oneOfBody(
      jsonBody[List[Todo]],
      htmlView[List[Todo]](views.todoList),
    ),
  )

val createTodoEndpoint = endpoint.post
  .in("todos")
  .in(jsonBody[CreateTodo])
  .out(jsonBody[Todo])

val deleteTodoEndpoint = endpoint.delete
  .in("todos" / path[String]("id"))
  .out(htmlView[Unit](_ => scalatags.Text.all.div()))

def todoEndpoints(
    service: TodoService,
): List[ServerEndpoint[Any, Identity]] =
  List(
    todosEndpoint.handleSuccess(_ => service.getActiveTodos()),
    createTodoEndpoint.handleSuccess(createTodo =>
      service.createTodo(createTodo),
    ),
    deleteTodoEndpoint.handleSuccess(id =>
      service.deleteTodo(TodoId(Ulid.from(id))),
    ),
  )
