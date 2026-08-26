package gls

import scala.language.experimental.saferExceptions

import io.vertx.core.*
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.{LoggerHandler, SessionHandler}
import io.vertx.ext.web.sstore.LocalSessionStore
import neotype.unwrap
import org.slf4j.LoggerFactory

import gls.controllers.*
import gls.domain.user.Email

class AppRouter(
    config: AppConfig,
    vertx: Vertx,
    services: Services,
) {

  private val logger = LoggerFactory.getLogger(classOf[AppRouter])

  private val userRoutesPrefix = "/user"

  def createRouter(): Router = {
    val router = Router.router(vertx)
    val templates = HandlebarsTemplates.create()

    val authManager = SessionAuthManager(
      services.user,
      s"${userRoutesPrefix}/login",
      s"${userRoutesPrefix}/me",
    )

    router
      .route()
      .handler(SessionHandler.create(LocalSessionStore.create(vertx)))

    val landingRoutes = LandingController.createRoutes(vertx, templates)
    val userRoutes =
      UserController(userRoutesPrefix).createRoutes(
        vertx,
        templates,
        services.user,
        authManager,
      )

    router.route().handler(LoggerHandler.create())
    router.route("/*").subRouter(landingRoutes)
    router.route(s"${userRoutesPrefix}/*").subRouter(userRoutes)

    router
  }
}

class AppVerticle(config: AppConfig) extends VerticleBase {

  private val logger = LoggerFactory.getLogger(classOf[AppVerticle])

  override def start(): Future[HttpServer] = {
    val services = Services.create()
    val appRouter = AppRouter(config, vertx, services)

    // TODO: Make this nicer/safer
    if (config.env == "dev") {
      try {
        services.user.createUser(Email("asd@asd"), PlainPassword("12345678"))
      } catch {
        case error: Exception =>
          logger.error("Failed to create demo user", error)
      }
    }

    vertx
      .createHttpServer()
      .requestHandler(appRouter.createRouter())
      .listen(config.web.port.unwrap)
      .onSuccess { server =>
        logger.info(s"HTTP server started at port: ${server.actualPort()}")
      }
      .onFailure { error =>
        logger.error(s"Server failure: ${error.getMessage}", error)
      }
  }

}
