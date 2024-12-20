package org.sarden.web

import java.nio.charset.StandardCharsets
import java.time.{LocalTime, ZoneId}

import scala.concurrent.duration.*
import scala.io.Source
import scala.language.postfixOps
import scala.util.Using

import com.github.f4b6a3.ulid.Ulid
import ox.*
import scalatags.Text
import scalatags.Text.all.*
import scalikejdbc.ConnectionPool
import sttp.model.{Header, MediaType}
import sttp.tapir.*
import sttp.tapir.json.upickle.*
import sttp.tapir.server.netty.sync.NettySyncServer

import org.sarden.core.*
import org.sarden.core.domain.todo.*
import org.sarden.core.domain.todo.internal.LiveTodoRepo

def htmlViewCodec[T: Schema](
    renderer: T => Text.TypedTag[String],
): Codec[String, T, CodecFormat.TextHtml] =
  Codec.anyString(CodecFormat.TextHtml())(_ =>
    throw new Exception("HTML input body is not supported"),
  )(value => renderer(value).render)

def htmlView[T: Schema](
    renderer: T => Text.TypedTag[String],
): EndpointIO.Body[String, T] =
  EndpointIO.Body(
    RawBodyType.StringBody(StandardCharsets.UTF_8),
    htmlViewCodec(renderer),
    EndpointIO.Info.empty,
  )

val todosEndpoint = endpoint.get
  .in("todos")
  .out(
    oneOfBody(
      jsonBody[List[Todo]],
      htmlView[List[Todo]](views.todoList),
    ),
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

// TODO: Ulid id type
val deleteTodoEndpoint = endpoint.delete
  .in("todos" / path[String]("id"))
  .out(htmlView[Unit](_ => scalatags.Text.all.div()))

val cssAssetsEndpoint = endpoint.get
  .in("assets" / "css" / path[String]("name"))
  .out(sttp.tapir.header(Header.contentType(MediaType.TextCss)))
  .out(stringBody(StandardCharsets.UTF_8))

val jsAssetsEndpoint = endpoint.get
  .in("assets" / "js" / path[String]("name"))
  .out(sttp.tapir.header(Header.contentType(MediaType.TextJavascript)))
  .out(stringBody(StandardCharsets.UTF_8))

// TODO: Load app config
object Main:
  def main(args: Array[String]): Unit =
    Class.forName("org.sqlite.JDBC")
    val dbUrl = "jdbc:sqlite:dev.db"
    ConnectionPool.singleton(dbUrl, "", "")
    val migrator = LiveMigrator(dbUrl)
    val idGenerator = LiveIdGenerator()
    val clock = LiveClock(ZoneId.of("UTC"))
    val todoRepo = LiveTodoRepo(clock, idGenerator)
    val todoService = LiveTodoService(todoRepo)

    migrator.migrate()

    val server = NettySyncServer()
      .port(8080)
      .addEndpoint(
        todosEndpoint.handleSuccess(_ => todoService.getActiveTodos()),
      )
      .addEndpoint(
        createTodoEndpoint.handleSuccess(createTodo =>
          todoService.createTodo(createTodo),
        ),
      )
      .addEndpoint(
        deleteTodoEndpoint.handleSuccess(id =>
          todoService.deleteTodo(TodoId(Ulid.from(id))),
        ),
      )
      .addEndpoint(
        cssAssetsEndpoint.handleSuccess { name =>
          Using(Source.fromURL(getClass.getResource(s"/assets/css/${name}")))(
            _.mkString(""),
          ).get
        },
      )
      .addEndpoint(
        jsAssetsEndpoint.handleSuccess { name =>
          Using(Source.fromURL(getClass.getResource(s"/assets/js/${name}")))(
            _.mkString(""),
          ).get
        },
      )

    supervised {
      val serverBinding = useInScope(server.start())(_.stop())

      println(
        s"Server is running on: ${serverBinding.hostName}:${serverBinding.port}",
      )

      never
    }
