package gls.controllers

import io.vertx.core.Vertx
import io.vertx.ext.web.Router

import gls.Templates

object LandingController extends BaseController("/") {

  def createRoutes(vertx: Vertx, templates: Templates): Router = {
    val router = Router.router(vertx)

    router.get("/").handler { implicit context =>
      respond(templates.render("index"))
    }

    router
  }

}
