package gls

import java.nio.file.Path

import io.vertx.core.*
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import neotype.unwrap
import org.slf4j.LoggerFactory
import pureconfig.ConfigSource

import gls.controllers.LandingController

class AppVerticle(config: AppConfig) extends VerticleBase {

  private val logger = LoggerFactory.getLogger(classOf[AppVerticle])

  override def start(): Future[HttpServer] = {
    val router = Router.router(vertx)
    val templates = JteTemplates(Path.of("web/src/main/resources/templates"))

    val landingRoutes = LandingController.createRoutes(vertx, templates)

    router.route().subRouter(landingRoutes)

    vertx
      .createHttpServer()
      .requestHandler(router)
      .listen(config.web.port.unwrap)
      .onSuccess { server =>
        logger.info(s"HTTP server started at port: ${server.actualPort()}")
      }
      .onFailure(error => error.printStackTrace())
  }

}

@main
def main(): Unit = {
  val logger = LoggerFactory.getLogger("main")

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
