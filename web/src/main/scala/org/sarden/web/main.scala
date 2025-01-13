package org.sarden.web

import java.nio.charset.StandardCharsets
import java.time.{Instant, LocalTime, ZoneId}

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
import upickle.default.ReadWriter

import org.sarden.core.*
import org.sarden.core.domain.todo.*
import org.sarden.core.domain.todo.internal.LiveTodoRepo
import org.sarden.core.domain.weather.*
import org.sarden.core.domain.weather.internal.LiveWeatherRepo

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
given Schema[Temperature] = Schema.anyObject
given Schema[SensorId] = Schema.anyObject
given Schema[WeatherMeasurement] = Schema.derived
given Schema[EmptyResponse] = Schema.derived
given Codec[String, SensorId, CodecFormat.TextPlain] =
  Codec.string.map(Mapping.from(SensorId(_))(_.unwrap))

case class EmptyResponse() derives ReadWriter

val createTodoEndpoint = endpoint.post
  .in("todos")
  .in(jsonBody[CreateTodo])
  .out(jsonBody[Todo])

val addWeatherMeasurementEndpoint = endpoint.post
  .in("weather")
  .in(jsonBody[Vector[WeatherMeasurement]])
  .out(jsonBody[EmptyResponse])

val getWeatherMeasurementsEndpoint = endpoint.get
  .in("weather")
  .in(query[Option[Instant]]("from"))
  .in(query[Option[Instant]]("to"))
  .in(query[Option[SensorId]]("sensor_id"))
  .out(jsonBody[List[WeatherMeasurement]])

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
    val weatherRepo = LiveWeatherRepo()
    val weatherService = LiveWeatherService(weatherRepo)

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
      .addEndpoint(
        addWeatherMeasurementEndpoint.handleSuccess { measurements =>
          weatherService.addMeasurements(measurements)
          EmptyResponse()
        },
      )
      .addEndpoint(
        getWeatherMeasurementsEndpoint.handleSuccess { (from, to, source) =>
          weatherService
            .getMeasurements(GetMeasurementsFilters(from, to, source))
        },
      )

    supervised {
      val serverBinding = useInScope(server.start())(_.stop())

      println(
        s"Server is running on: ${serverBinding.hostName}:${serverBinding.port}",
      )

      never
    }
