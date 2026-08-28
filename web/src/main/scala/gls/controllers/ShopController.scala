package gls.controllers

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import neotype.unwrap

import gls.Templates
import gls.domain.shop.*

class ShopController(private val routePrefix: String)
    extends BaseController(routePrefix) {

  private val currency = "HUF"

  def createRoutes(
      vertx: Vertx,
      templates: Templates,
      shopService: ShopService,
      auth: SessionAuthManager,
  ): Router = {
    given router: Router = Router.router(vertx)

    router.get("/products").handler { implicit context =>
      val products = shopService.listProducts(ProductQuery.empty())

      respond(
        templates.render(
          "shop/products",
          ProductsVM(
            products.items.map { product =>
              ProductVM(
                product.id.unwrap.toString,
                product.name.unwrap,
                product.scientificName.unwrap,
                formatPrice(product.price, product.saleUnit),
              )
            }.toArray,
            products.statistics.totalByCategory.map { (category, total) =>
              CategoryVM(category.toString, total)
            }.toArray,
          ),
        ),
      )
    }

    router
  }

  private def formatPrice(price: Price, saleUnit: SaleUnit): String = {
    val unit = saleUnit match {
      case SaleUnit.Kg     => "Kg"
      case SaleUnit.Bag500 => "500g bag"
      case SaleUnit.Bag250 => "250g bag"
      case SaleUnit.Bunch  => "Bunch"
    }

    s"${price.unwrap} ${currency} / ${unit}"
  }

}

case class ProductVM(
    id: String,
    name: String,
    scientificName: String,
    price: String,
)
case class CategoryVM(name: String, total: Int)
case class ProductsVM(products: Array[ProductVM], categories: Array[CategoryVM])
