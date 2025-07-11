package org.sarden.web

import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.json.zio.*
import sttp.tapir.server.model.ValuedEndpointOutput
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import sttp.tapir.ztapir.ZServerEndpoint
import zio.*
import zio.http.{Response as ZioHttpResponse, Routes, Server}

import org.sarden.*
import org.sarden.bindings.Migrator
import org.sarden.domain.plant.PlantService
import org.sarden.domain.user.UserService
import org.sarden.{CoreConfig, CoreServices}

type AppServerEndpoint = ZServerEndpoint[CoreServices, ZioStreams & WebSockets]

object Main extends ZIOAppDefault:

  override def run: ZIO[Any & ZIOAppArgs & Scope, Any, Any] =
    (for
      migrator <- ZIO.service[Migrator]
      config <- ZIO.service[WebConfig]
      _ <- ZIO.attemptBlocking:
        Class.forName("org.sqlite.JDBC")
      _ <- migrator.backup()
      _ <- migrator.migrate()
      apiRoutes =
        if config.serverMode != ServerMode.OnlySite then
          routes.api.apiRoutes(using config.apiAuthConfig)
        else List.empty
      siteRoutes =
        if config.serverMode != ServerMode.OnlyApi then
          routes.pages.pageRoutes(using config.siteAuthConfig)
        else List.empty
      allRoutes: Routes[
        CoreServices,
        ZioHttpResponse,
      ] = ZioHttpInterpreter(
        // TODO: Not great, sould return http for pages and json for apis
        ZioHttpServerOptions.customiseInterceptors
          .defaultHandlers(message =>
            ValuedEndpointOutput(
              jsonBody[String],
              s"Something went really wrong: ${message}",
            ),
          )
          .options,
      ).toHttp[CoreServices](
        apiRoutes ++ siteRoutes,
      )
      exitCode <- Server.serve(allRoutes)
    yield exitCode).provide(
      ZLayer.succeed(Server.Config.default.port(8080)),
      Server.live,
      CoreConfig.live,
      WebConfig.live,
      CoreServices.live,
    )
