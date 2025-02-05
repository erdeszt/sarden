package org.sarden.web

import scala.language.postfixOps

import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.ztapir.ZServerEndpoint
import zio.*
import zio.http.{Response as ZioHttpResponse, Routes, Server}

import org.sarden.core.*
import org.sarden.core.domain.plant.PlantService
import org.sarden.core.domain.todo.TodoService
import org.sarden.core.domain.weather.WeatherService

type AppServerEndpoint = ZServerEndpoint[CoreServices, ZioStreams & WebSockets]
type CoreServices = TodoService & PlantService & WeatherService

object Main extends ZIOAppDefault:

  override def run: ZIO[Any & ZIOAppArgs & Scope, Any, Any] =
    (for
      migrator <- ZIO.service[Migrator]
      _ <- ZIO.attemptBlocking {
        Class.forName("org.sqlite.JDBC")
      }
      _ <- migrator.migrate()
      routes: Routes[
        CoreServices,
        ZioHttpResponse,
      ] = ZioHttpInterpreter()
        .toHttp[CoreServices](
          List(
            endpoints.cssAssetsServerEndpoint,
            endpoints.jsAssetsServerEndpoint,
          ) ++ endpoints.todoEndpoints ++ endpoints.weatherEndpoints ++ endpoints.plantEndpoints,
        )
      exitCode <- Server
        .serve(routes)
    yield exitCode).provide(
      ZLayer.succeed(Server.Config.default.port(8080)),
      Server.live,
      CoreConfig.live,
      wireLive,
    )
