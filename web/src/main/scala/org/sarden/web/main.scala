package org.sarden.web

import java.time.ZoneId

import scala.language.postfixOps

import scalikejdbc.ConnectionPool
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import zio.*
import zio.http.{Response as ZioHttpResponse, Routes, Server}

import org.sarden.core.*

object Main extends ZIOAppDefault:

  override def run: ZIO[Any & ZIOAppArgs & Scope, Any, Any] =
    val dbUrl = "jdbc:sqlite:dev.db"
    val coreConfig = CoreConfig(
      ZoneId.of("UTC"),
      dbUrl,
    )
    for
      _ <- ZIO.attemptBlocking {
        Class.forName("org.sqlite.JDBC")
        ConnectionPool.singleton(dbUrl, "", "")
      }
      services = wireLive(coreConfig)
      _ <- services.migrator.migrate()
      routes: Routes[Any, ZioHttpResponse] = ZioHttpInterpreter().toHttp(
        List(
          endpoints.cssAssetsServerEndpoint,
          endpoints.jsAssetsServerEndpoint,
        ) ++ endpoints.todoEndpoints(services.todo) ++ endpoints
          .weatherEndpoints(services.weather) ++ endpoints.plantEndpoints(
          services.plant,
        ),
      )
      exitCode <- Server
        .serve(routes)
        .provide(ZLayer.succeed(Server.Config.default.port(8080)), Server.live)
    yield exitCode
