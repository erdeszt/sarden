package gls

import io.vertx.core.*
import org.slf4j.LoggerFactory
import pureconfig.ConfigSource

object Main {

  private val logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
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
        ()
    }
  }

}
