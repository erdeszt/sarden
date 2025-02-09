package org.sarden.web

import scala.language.postfixOps

import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.ztapir.ZServerEndpoint
import zio.*
import zio.http.{Response as ZioHttpResponse, Routes, Server}

import org.sarden.core.*
import org.sarden.core.plant.PlantService
import org.sarden.core.todo.TodoService
import org.sarden.core.weather.WeatherService

type AppServerEndpoint = ZServerEndpoint[CoreServices, ZioStreams & WebSockets]
type CoreServices = TodoService & PlantService & WeatherService

object Main extends ZIOAppDefault:

  override def run: ZIO[Any & ZIOAppArgs & Scope, Any, Any] =
    (for
      migrator <- ZIO.service[Migrator]
      _ <- ZIO.attemptBlocking:
        Class.forName("org.sqlite.JDBC")
      _ <- migrator.migrate()
      allRoutes: Routes[
        CoreServices,
        ZioHttpResponse,
      ] = ZioHttpInterpreter().toHttp[CoreServices](
        routes.api.apiRoutes ++ routes.pages.pageRoutes,
      )
      exitCode <- Server.serve(allRoutes)
    yield exitCode).provide(
      ZLayer.succeed(Server.Config.default.port(8080)),
      Server.live,
      CoreConfig.live,
      wireLive,
    )
