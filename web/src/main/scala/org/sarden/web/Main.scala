package org.sarden.web

import java.time.{LocalTime, ZoneId}
import java.util.concurrent.TimeUnit

import scala.concurrent.duration.FiniteDuration

import scalikejdbc.{AutoSession, ConnectionPool, DBSession}
import sttp.tapir.*
import sttp.tapir.json.upickle.*
import sttp.tapir.server.netty.sync.NettySyncServer

import org.sarden.core.*
import org.sarden.core.domain.todo.*
import org.sarden.core.domain.todo.internal.LiveTodoRepo

// TODO: Html + json output
val todosEndpoint = endpoint.get
  .in("todos")
  .out(
    jsonBody[List[Todo]],
//    oneOfBody(
//      jsonBody[List[Todo]],
//      htmlRenderer[List[Todo]],
//    ),
  )

given Schema[Schedule] = Schema.derived
given Schema[FiniteDuration] = Schema.anyObject
given Schema[TodoId] = Schema.string
given Schema[TodoName] = Schema.string
given Schema[CreateTodo] = Schema.derived
given Schema[Todo] = Schema.derived

val createTodoEndpoint = endpoint.post
  .in("todos")
  .in(jsonBody[CreateTodo])
  .out(jsonBody[Todo])

// TODO: Load app config
object Main:
  def main(args: Array[String]): Unit =
    Class.forName("org.sqlite.JDBC")
    val dbUrl = "jdbc:sqlite:dev.db"
    ConnectionPool.singleton(dbUrl, "", "")
    given session: DBSession = AutoSession
    val migrator = LiveMigrator(dbUrl)
    val idGenerator = LiveIdGenerator()
    val clock = LiveClock(ZoneId.of("UTC"))
    val todoRepo = LiveTodoRepo(clock, idGenerator)
    val todoService = LiveTodoService(todoRepo)

    val x = CreateTodo(
      TodoName("test"),
      Schedule.EverySecondFridayOfTheMonth(LocalTime.of(15, 0)),
      FiniteDuration(24, TimeUnit.HOURS),
    )

    println(s"\n\n${upickle.default.write(x)}\n")

    migrator.migrate()

    // TODO: Manage server lifetime/resource with ox
    // See: https://github.com/softwaremill/tapir/blob/master/examples/src/main/scala/sttp/tapir/examples/helloWorldNettySyncServer.scala#L31
    NettySyncServer()
      .port(8080)
      .addEndpoint(
        todosEndpoint.handleSuccess(_ => todoService.getActiveTodos()),
      )
      .addEndpoint(
        createTodoEndpoint.handleSuccess(createTodo =>
          todoService.createTodo(createTodo),
        ),
      )
      .startAndWait()
