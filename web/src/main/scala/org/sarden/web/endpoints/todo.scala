package org.sarden.web.endpoints

import scala.concurrent.duration.FiniteDuration

import com.github.f4b6a3.ulid.Ulid
import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.Schema
import sttp.tapir.json.upickle.*
import sttp.tapir.ztapir.*

import org.sarden.core.domain.todo.*
import org.sarden.web.*

given Schema[TodoSchedule] = Schema.derived
given Schema[FiniteDuration] = Schema.anyObject
given Schema[TodoId] = Schema.string
given Schema[TodoName] = Schema.string
given Schema[CreateTodo] = Schema.derived
given Schema[Todo] = Schema.derived

val todosEndpoint = endpoint.get
  .in("todos")
  .out(
    oneOfBody(
      jsonBody[Vector[Todo]],
      htmlView[Vector[Todo]](views.todoList),
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
): List[ZServerEndpoint[Any, ZioStreams & WebSockets]] =
  List(
    todosEndpoint.zServerLogic(_ => service.getActiveTodos()),
    createTodoEndpoint.zServerLogic(createTodo =>
      service.createTodo(createTodo),
    ),
    deleteTodoEndpoint.zServerLogic(id =>
      service.deleteTodo(TodoId(Ulid.from(id))),
    ),
  )
