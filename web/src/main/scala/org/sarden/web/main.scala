package org.sarden.web

import java.time.ZoneId

import scala.language.postfixOps

import ox.*
import scalikejdbc.ConnectionPool
import sttp.tapir.*
import sttp.tapir.server.netty.sync.NettySyncServer

import org.sarden.core.*

object Main:
  def main(args: Array[String]): Unit =
    Class.forName("org.sqlite.JDBC")
    val dbUrl = "jdbc:sqlite:dev.db"
    ConnectionPool.singleton(dbUrl, "", "")
    val coreConfig = CoreConfig(
      ZoneId.of("UTC"),
      dbUrl,
    )
    val services = wireLive(coreConfig)

    services.migrator.migrate()

    val server = NettySyncServer()
      .port(8080)
      .addEndpoint(endpoints.cssAssetsServerEndpoint)
      .addEndpoint(endpoints.jsAssetsServerEndpoint)
      .addEndpoints(endpoints.todoEndpoints(services.todo))
      .addEndpoints(endpoints.plantEndpoints(services.plant))
      .addEndpoints(endpoints.weatherEndpoints(services.weather))

    supervised {
      val serverBinding = useInScope(server.start())(_.stop())

      println(
        s"Server is running on: ${serverBinding.hostName}:${serverBinding.port}",
      )

      never
    }
