package gls.controllers

import scala.language.experimental.saferExceptions

import io.vertx.core.{Handler, Vertx}
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import neotype.*
import org.slf4j.LoggerFactory

import gls.*
import gls.domain.user.*

// TODO: Localization
class UserController(private val routePrefix: String)
    extends RouteHelper(routePrefix) {

  private val logger = LoggerFactory.getLogger(getClass)

  def createRoutes(
      vertx: Vertx,
      templates: Templates,
      userService: UserService,
      auth: SessionAuthManager,
  ): Router = {
    given router: Router = Router.router(vertx)

    router.get("/login").handler(auth.loggedInRedirectHandler).handler {
      context =>
        context.end(templates.render("user/login"))
    }

    router
      .post("/login")
      .handler(BodyHandler.create())
      .handler(auth.loggedInRedirectHandler)
      .handler { implicit context =>
        val rawEmail = context.request().getFormAttribute("email")
        val rawPassword = context.request().getFormAttribute("password")

        val user = userService.getByCredentials(
          Email(rawEmail),
          PlainPassword(rawPassword),
        )

        user match {
          case None =>
            context.response().setStatusCode(400) // TODO: Use enum
            context.end(
              templates
                .render("user/login", LoginPage(Array("Invalid credentials"))),
            )
          case Some(loggedInUser) =>
            auth.login(loggedInUser.id)
            context.redirect(route("me"))
        }
      }

    router.get("/logout").handler { implicit context =>
      auth.logout()
      context.redirect(route("login"))
    }

    router.get("/signup").handler(auth.loggedInRedirectHandler).handler {
      context =>
        context.end(templates.render("user/signup"))
    }

    router
      .post("/signup")
      .handler(BodyHandler.create())
      .handler(auth.loggedInRedirectHandler)
      .handler { implicit context =>
        val rawEmail = context.request().getFormAttribute("email")
        val rawPassword = context.request().getFormAttribute("password")
        val rawRepeatPassword =
          context.request().getFormAttribute("repeat_password")

        if (rawPassword != rawRepeatPassword) {
          context.response().setStatusCode(400)
          context.end(
            templates.render(
              "user/signup",
              SignupPage(Array("Passwords don't match")),
            ),
          )
        } else {
          try {
            val user = userService.createUser(
              Email(rawEmail),
              PlainPassword(rawPassword),
            )

            auth.login(user.id)

            context.redirect(route("me"))
          } catch {
            case _: EmailFormatError =>
              context.response().setStatusCode(400)
              context.end(
                templates.render(
                  "user/signup",
                  SignupPage(Array("Email format invalid")),
                ),
              )
            case _: WeakPasswordError =>
              context.response().setStatusCode(400)
              context.end(
                templates.render(
                  "user/signup",
                  SignupPage(Array("Password is too weak")),
                ),
              )
          }
        }
      }

    auth.route[UserRole.Basic](_.get("/me")) { (context, userCtx) =>
      val me = userService.getSelf(using userCtx)

      context.end(
        templates.render(
          "user/me",
          MePage(me.id.unwrap.toString, me.email.unwrap),
        ),
      )
    }

    auth.route[UserRole.Admin](_.get("/admin")) { (context, userCtx) =>
      val me = userService.getSelf(using userCtx)

      context.end(
        templates.render(
          "user/me",
          MePage(s"${me.id.unwrap}[Admin]", me.email.unwrap),
        ),
      )
    }

    router
  }

}

case class SignupPage(formErrors: Array[String])
case class LoginPage(formErrors: Array[String])
case class MePage(userId: String, email: String)
