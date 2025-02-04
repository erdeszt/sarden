package org.sarden.web.endpoints

import scala.concurrent.duration.FiniteDuration

import com.github.f4b6a3.ulid.Ulid
import sttp.tapir.Schema
import sttp.tapir.json.upickle.*
import sttp.tapir.ztapir.*
import zio.*

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
