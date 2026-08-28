package gls.controllers

import com.github.f4b6a3.ulid.Ulid
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import neotype.unwrap
import gls.Templates
import gls.domain.shop.*
import gls.domain.user.UserRole

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
      val products = shopService.listProducts(ProductQuery.empty())

      respond(
        templates.render(
          "shop/products",
          ProductsVM(
            products.items.map(_.toVM()).toArray,
            products.statistics.totalByCategory.map { (category, total) =>
              CategoryVM(category.toString, total)
            }.toArray,
          ),
        ),
      )
    }

    router.get("/product/:productId").handler { implicit context =>
      val rawProductId = context.pathParam("productId")

      try {
        val productId = ProductId(Ulid.from(rawProductId))

        shopService.getProductById(productId) match {
          case None          => redirect(route("products"))
          case Some(product) =>
            respond(
              templates.render(
                "shop/product",
                product.toVM()
              ),
            )
        }
      } catch {
        case _: Exception => redirect(route("products"))
      }
    }

    val _ = auth.route[UserRole.Basic](_.post("/cart").handler(BodyHandler.create())) { implicit (routingCtx, _) =>
      val rawProductId = routingCtx.request().getFormAttribute("productId")
      val rawQuantity = routingCtx.request().getFormAttribute("quantity")

      // TODO: Nicer error handling
      try {
        val productId = ProductId(Ulid.from(rawProductId))
        val quantity = Quantity(rawQuantity.toInt)

        shopService.addToCart(productId, quantity)

        redirect(route(s"product/${productId.unwrap.toString}"))
      } catch {
        case _: Exception => redirect(route("products"))
      }
    }

    val _ = auth.route[UserRole.Basic](_.get("/cart")) { implicit (_, _) =>
      val cart = shopService.getCart()

      respond(templates.render("shop/cart", CartVM(cart.items.map { item =>
        CartItemVM(item.product.toVM(), item.quantity.unwrap)
      }.toArray)))
    }

    router
  }

}

extension (product: Product)

  private inline def currency = "HUF"

  private def formatPrice(price: Price, saleUnit: SaleUnit): String = {
    val unit = saleUnit match {
      case SaleUnit.Kg     => "Kg"
      case SaleUnit.Bag500 => "500g bag"
      case SaleUnit.Bag250 => "250g bag"
      case SaleUnit.Bunch  => "Bunch"
    }

    s"${price.unwrap} ${currency} / ${unit}"
  }

  inline def toVM(): ProductVM = {
    ProductVM(
      product.id.unwrap.toString,
      product.name.unwrap,
      product.description.unwrap,
      product.scientificName.unwrap,
      formatPrice(product.price, product.saleUnit),
    )
  }

case class ProductVM(
    id: String,
    name: String,
    description: String,
    scientificName: String,
    price: String,
)
case class CategoryVM(name: String, total: Int)
case class ProductsVM(products: Array[ProductVM], categories: Array[CategoryVM])
case class CartVM(items: Array[CartItemVM])
case class CartItemVM(product: ProductVM, quantity: Int)
