package gls.controllers

import scala.language.experimental.saferExceptions

import io.vertx.core.{Handler, Vertx}
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import neotype.*

import gls.*
import gls.domain.user.*

// TODO: Localization
class UserController(private val routePrefix: String)
    extends BaseController(routePrefix) {

  def createRoutes(
      vertx: Vertx,
      templates: Templates,
      userService: UserService,
      auth: SessionAuthManager,
  ): Router = {
    given router: Router = Router.router(vertx)

    router.get("/login").handler(auth.loggedInRedirectHandler).handler {
      implicit context =>
        respond(templates.render("user/login"))
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
            respond(
              templates
                .render("user/login", LoginPage(Array("Invalid credentials"))),
              statusCode = 400,
            )
          case Some(loggedInUser) =>
            auth.login(loggedInUser.id)
            redirect(route("me"))
        }
      }

    router.get("/logout").handler { implicit context =>
      auth.logout()
      redirect(route("login"))
    }

    router.get("/signup").handler(auth.loggedInRedirectHandler).handler {
      implicit context =>
        respond(templates.render("user/signup"))
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
          respond(
            templates.render(
              "user/signup",
              SignupPage(Array("Passwords don't match")),
            ),
            statusCode = 400,
          )
        } else {
          try {
            val user = userService.createUser(
              Email(rawEmail),
              PlainPassword(rawPassword),
            )

            auth.login(user.id)

            redirect(route("me"))
          } catch {
            case _: EmailFormatError =>
              respond(
                templates.render(
                  "user/signup",
                  SignupPage(Array("Email format invalid")),
                ),
                statusCode = 400,
              )
            case _: WeakPasswordError =>
              respond(
                templates.render(
                  "user/signup",
                  SignupPage(Array("Password is too weak")),
                ),
                statusCode = 400,
              )
          }
        }
      }

    val _ = auth.route[UserRole.Basic](_.get("/me")) { implicit (_, userCtx) =>
      val me = userService.getSelf(using userCtx)

      respond(
        templates.render(
          "user/me",
          MePage(me.id.unwrap.toString, me.email.unwrap),
        ),
      )
    }

    val _ = auth.route[UserRole.Admin](_.get("/admin")) { implicit (_, userCtx) =>
      val me = userService.getSelf(using userCtx)

      respond(
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
