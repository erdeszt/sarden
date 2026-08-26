package gls

import java.nio.file.Path
import scala.language.experimental.saferExceptions
import io.vertx.core.*
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.{LoggerHandler, SessionHandler}
import neotype.unwrap
import org.slf4j.LoggerFactory
import gls.controllers.*
import io.vertx.ext.web.sstore.LocalSessionStore

class AppVerticle(config: AppConfig) extends VerticleBase {

  private val logger = LoggerFactory.getLogger(classOf[AppVerticle])

  override def start(): Future[HttpServer] = {
    val router = Router.router(vertx)
    val templates = HandlebarsTemplates.create()

    val services = Services.create()
    
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)))

    val landingRoutes = LandingController.createRoutes(vertx, templates)
    val userRoutes =
      UserController.createRoutes(vertx, templates, services.user)

    router.route().handler(LoggerHandler.create())
    router.route("/*").subRouter(landingRoutes)
    router.route("/user/*").subRouter(userRoutes)

    vertx
      .createHttpServer()
      .requestHandler(router)
      .listen(config.web.port.unwrap)
      .onSuccess { server =>
        logger.info(s"HTTP server started at port: ${server.actualPort()}")
      }
      .onFailure { error =>
        logger.error(s"Server failure: ${error.getMessage}", error)
      }
  }

}
