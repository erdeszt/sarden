package gls.controllers

import scala.language.experimental.saferExceptions
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.{BodyHandler, SessionHandler}
import neotype.*
import org.slf4j.LoggerFactory
import gls.*
import gls.domain.user.*
import io.vertx.ext.web.sstore.LocalSessionStore

// TODO: Localization
// TODO: Auth session(login page, me page)
object UserController {

  private val logger = LoggerFactory.getLogger(UserController.getClass)

  def createRoutes(
      vertx: Vertx,
      templates: Templates,
      userService: UserService,
  ): Router = {
    val router = Router.router(vertx)
    
    router.get("/login").handler { context =>
      context.end(templates.render("user/login"))
    }

    router.post("/login").handler(BodyHandler.create()).handler { context =>
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
          context.redirect("/user/me")
      }
    }

    router.get("/signup").handler { context =>
      context.end(templates.render("user/signup"))
    }

    router.post("/signup").handler(BodyHandler.create()).handler { context =>
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

          context.redirect("/user/login")
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

    router.get("/me").handler { context =>
      if (context.session().get("user_id") == null) {
        context.redirect("/user/login")
      } else {
        context.end(templates.render("user/me"))
      }
    }

    router
  }

}

case class SignupPage(formErrors: Array[String])
case class LoginPage(formErrors: Array[String])
