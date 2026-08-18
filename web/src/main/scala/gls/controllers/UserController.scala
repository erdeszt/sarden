package gls.controllers

import gls.Templates
import gls.domain.user.UserService
import io.vertx.core.Vertx
import io.vertx.ext.web.Router

object UserController {
  
  def createRoutes(vertx: Vertx, templates: Templates, userService: UserService): Router = {
    val router = Router.router(vertx)
    
    router.get("/login").handler(context => {
      context.end("Login page")
    })
    
    router
  }

}
