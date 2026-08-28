package gls.controllers

import io.vertx.core.Vertx
import io.vertx.ext.web.Router

import gls.Templates
import gls.domain.shop.*

class ShopController(private val routePrefix: String)
    extends BaseController(routePrefix) {

  def createRoutes(
      vertx: Vertx,
      templates: Templates,
      shopService: ShopService,
      auth: SessionAuthManager,
  ): Router = {
    given router: Router = Router.router(vertx)

    router.get("/products").handler { implicit context =>
      respond(templates.render("shop/products"))
    }

    router
  }

}

case class ShopIndexVM(products: Vector[Product])
