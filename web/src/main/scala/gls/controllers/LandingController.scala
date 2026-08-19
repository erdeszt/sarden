package gls.controllers

import io.vertx.core.Vertx
import io.vertx.ext.web.Router

import gls.Templates

object LandingController {

  def createRoutes(vertx: Vertx, templates: Templates): Router = {
    val router = Router.router(vertx)

    router.get("/").handler { context =>
      context.end(templates.render("index"))
    }

    router
  }

}
