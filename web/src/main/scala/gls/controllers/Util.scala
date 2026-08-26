package gls.controllers

import scala.language.experimental.saferExceptions
import scala.reflect.ClassTag

import com.github.f4b6a3.ulid.Ulid
import io.vertx.core.Handler
import io.vertx.ext.web.{Route, Router, RoutingContext}
import neotype.unwrap
import org.slf4j.LoggerFactory

import gls.domain.user.{UserCtx, UserId, UserRole, UserService}

class SessionAuthManager(
    userService: UserService,
    loginRoute: String,
    loggedInHomeRoute: String,
) {

  private val logger = LoggerFactory.getLogger(getClass)

  private val userIdSessionTag = "user_id"

  val loggedInRedirectHandler: Handler[RoutingContext] =
    new Handler[RoutingContext] {
      override def handle(context: RoutingContext): Unit = {
        if (context.session().get(userIdSessionTag) != null) {
          context.redirect(loggedInHomeRoute);
          ()
        } else {
          context.next()
        }
      }
    }

  def route[Role <: UserRole: ClassTag](routeBuilder: Router => Route)(
      routeHandler: (RoutingContext, UserCtx[Role]) => Unit,
  )(using router: Router): Route = {
    routeBuilder(router).handler { context =>
      val sessionUserId = Option(context.session().get[Ulid](userIdSessionTag))

      sessionUserId match {
        case None =>
          context.redirect(loginRoute);
          ()
        case Some(userId) =>
          val user =
            userService.getSelf(using UserCtx[UserRole.Basic](UserId(userId)))

          if (
            summon[ClassTag[Role]].runtimeClass.isAssignableFrom(
              user.role.getClass,
            )
          ) {
            logger.info(
              s"Unauthorized access attempt for: ${context.request().path()} by User(${userId})",
            )
            routeHandler(context, UserCtx[Role](UserId(userId)))
          } else {
            context.redirect(loggedInHomeRoute);
            ()
          }
      }
    }
  }

  def login(userId: UserId)(using context: RoutingContext): Unit = {
    context.session().put(userIdSessionTag, userId.unwrap);
    ()
  }

  def logout()(using context: RoutingContext): Unit = {
    context.session().remove(userIdSessionTag)
  }

}

trait BaseController(private val routePrefix: String) {

  def route(path: String): String = {
    s"${routePrefix}/${path}"
  }

  def respond(body: String, statusCode: Int = 200)(using
      context: RoutingContext,
  ): Unit = {
    context.response().setStatusCode(statusCode)
    context.end(body)
    ()
  }

  def redirect(target: String, statusCode: Int = 302)(using
      context: RoutingContext,
  ): Unit = {
    context.response().setStatusCode(statusCode)
    context.redirect(target)
    ()
  }

}
