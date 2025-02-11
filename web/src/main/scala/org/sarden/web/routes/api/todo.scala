package org.sarden.web.routes.api

import io.scalaland.chimney.dsl.*
import sttp.tapir.Schema
import sttp.tapir.json.zio.*
import sttp.tapir.ztapir.*
import zio.*
import zio.json.*

import org.sarden.core.mapping.{*, given}
import org.sarden.core.todo.*
import org.sarden.web.*

given JsonCodec[TodoSchedule] = JsonCodec.derived

given PartialTransformer[String, TodoSchedule] = PartialTransformer: raw =>
  Result.fromEitherString(raw.fromJson)

given Transformer[TodoSchedule, String] = _.toJson

// TODO: Use Request/Response Postfix
private[api] case class TodoDTO(
    id: String,
    name: String,
    schedule: String,
    notifyBefore: String,
    lastRun: Option[Long],
) derives JsonCodec,
      Schema

private[api] case class CreateTodoDTO(
    name: String,
    schedule: String,
    notifyBefore: String,
) derives JsonCodec,
      Schema

val todosEndpoint = baseEndpoint.get
  .in("todos")
  .out(
    oneOfBody(
      jsonBody[Vector[TodoDTO]],
    ),
  )

val createTodoEndpoint = baseEndpoint.post
  .in("todos")
  .in(jsonBody[CreateTodoDTO])
  .out(jsonBody[TodoDTO])

def todoEndpoints: List[AppServerEndpoint] =
  List(
    todosEndpoint.zServerLogic { _ =>
      ZIO.serviceWithZIO[TodoService]:
        _.getActiveTodos().map(_.map(_.transformInto[TodoDTO]))
    },
    createTodoEndpoint.zServerLogic { createTodoDto =>
      for
        createTodo <- createTodoDto.transformIntoPartialZIOOrDie[CreateTodo]
        todo <- ZIO.serviceWithZIO[TodoService](
          _.createTodo(createTodo).map(_.transformInto[TodoDTO]),
        )
      yield todo
    },
  )
