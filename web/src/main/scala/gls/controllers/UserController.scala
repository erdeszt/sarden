package gls.controllers

import scala.language.experimental.saferExceptions

import io.vertx.core.{Handler, Vertx}
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.{Router, RoutingContext}
import neotype.*
import org.slf4j.LoggerFactory

import gls.*
import gls.domain.user.*

// TODO: Localization
// TODO: Auth session(login page, me page)
class UserController(private val routePrefix: String) {

  private val logger = LoggerFactory.getLogger(getClass)

  private val userIdSessionTag = "user_id"

  class LoggedInRedirectHandler(userIdSessionTag: String, target: String)
      extends Handler[RoutingContext] {

    override def handle(context: RoutingContext): Unit = {
      if (context.session().get(userIdSessionTag) != null) {
        context.redirect(target)
      } else {
        context.next()
      }
    }
  }

  class SessionAuthHandler(userIdSessionTag: String, loginRoute: String)
      extends Handler[RoutingContext] {
    override def handle(context: RoutingContext): Unit = {
      if (context.session().get(userIdSessionTag) == null) {
        context.redirect(loginRoute)
      } else {
        context.next()
      }
    }
  }

  def createRoutes(
      vertx: Vertx,
      templates: Templates,
      userService: UserService,
  ): Router = {
    val router = Router.router(vertx)
    val loggedInRedirect =
      LoggedInRedirectHandler(userIdSessionTag, s"${routePrefix}/me")
    val sessionAuth =
      SessionAuthHandler(userIdSessionTag, s"${routePrefix}/login")

    router.get("/login").handler(loggedInRedirect).handler { context =>
      context.end(templates.render("user/login"))
    }

    router
      .post("/login")
      .handler(BodyHandler.create())
      .handler(loggedInRedirect)
      .handler { context =>
        val rawEmail = context.request().getFormAttribute("email")
        val rawPassword = context.request().getFormAttribute("password")

        val user = userService.getByCredentials(
          Email(rawEmail),
          PlainPassword(rawPassword),
        )

        user match {
          case None =>
            context.end(
              templates
                .render("user/login", LoginPage(Array("Invalid credentials"))),
            )
          case Some(loggedInUser) =>
            context.session.put("user_id", loggedInUser.id.unwrap)
            context.redirect(s"${routePrefix}/me")
        }
      }

    router.get("/signup").handler(loggedInRedirect).handler { context =>
      context.end(templates.render("user/signup"))
    }

    router
      .post("/signup")
      .handler(BodyHandler.create())
      .handler(loggedInRedirect)
      .handler { context =>
        val rawEmail = context.request().getFormAttribute("email")
        val rawPassword = context.request().getFormAttribute("password")
        val rawRepeatPassword =
          context.request().getFormAttribute("repeat_password")

        if (rawPassword != rawRepeatPassword) {
          context.end(
            templates.render(
              "user/signup",
              SignupPage(Array("Passwords don't match")),
            ),
          )
        } else {
          try {
            userService.createUser(Email(rawEmail), PlainPassword(rawPassword))

            context.redirect(s"${routePrefix}/login")
          } catch {
            case _: EmailFormatError =>
              context.end(
                templates.render(
                  "user/signup",
                  SignupPage(Array("Email format invalid")),
                ),
              )
            case _: WeakPasswordError =>
              context.end(
                templates.render(
                  "user/signup",
                  SignupPage(Array("Password is too weak")),
                ),
              )
          }
        }
      }

    router.get("/me").handler(sessionAuth) handler { context =>
      context.end(templates.render("user/me"))
    }

    router
  }

}

case class SignupPage(formErrors: Array[String])
case class LoginPage(formErrors: Array[String])
