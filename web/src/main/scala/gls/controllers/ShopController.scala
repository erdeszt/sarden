package gls.controllers

import io.vertx.core.Vertx
import io.vertx.ext.web.Router

import gls.Templates
import gls.domain.shop.ShopService

class ShopController(private val routePrefix: String)
    extends BaseController(routePrefix) {

  def createRoutes(
      vertx: Vertx,
      templates: Templates,
      shopService: ShopService,
      auth: SessionAuthManager,
  ): Router = {
    given router: Router = Router.router(vertx)

    router
  }

}
