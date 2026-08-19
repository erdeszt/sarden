package gls

import scala.language.experimental.saferExceptions

import io.vertx.core.*
import org.slf4j.LoggerFactory
import pureconfig.ConfigSource

object Main {

  def main(args: Array[String]): Unit = {
    val logger = LoggerFactory.getLogger(Main.getClass)

    ConfigSource.default.load[AppConfig] match {
      case Left(error) =>
        logger.error(s"Failed to load configuration ${error}")
      case Right(appConfig) =>
        val vertx = Vertx.vertx()
        val app = AppVerticle(appConfig)

        vertx
          .deployVerticle(
            app,
            DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD),
          )
          .await()
    }
  }

}
