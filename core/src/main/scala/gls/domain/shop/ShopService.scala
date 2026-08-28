package gls.domain.shop

import scala.collection.concurrent.TrieMap

import com.github.f4b6a3.ulid.Ulid

import gls.domain.user.{UserCtx, UserId, UserRole}
import gls.{Clock, IdGenerator}

case class ProductList(items: Vector[Product], statistics: ProductStatistics)

trait ShopService {
  def listProducts(query: ProductQuery): ProductList
  def getProductById(productId: ProductId): Option[Product]
  def addToCart(productId: ProductId, quantity: Quantity)(using
      UserCtx[UserRole.Basic],
  ): Unit
  def getCart()(using UserCtx[UserRole.Basic]): Cart
}

object ShopService {
  def live(
      idGenerator: IdGenerator[Ulid],
      clock: Clock,
  ): ShopService = {
    LiveShopService(
      InMemoryShopRepo(),
      idGenerator,
      clock,
    )
  }
}

class LiveShopService(
    shopRepo: ShopRepo,
    idGenerator: IdGenerator[Ulid],
    clock: Clock,
) extends ShopService {

  private val carts = TrieMap.empty[UserId, TrieMap[ProductId, Quantity]]

  override def listProducts(query: ProductQuery): ProductList = {
    val totalByCategory =
      ProductCategory.all.foldLeft(Map.empty[ProductCategory, Int]) {
        (stats, category) =>
          stats + (category -> products.filter(_.category == category).size)
      }

    ProductList(
      products,
      ProductStatistics(
        totalByCategory,
      ),
    )
  }

  override def getProductById(productId: ProductId): Option[Product] = {
    products.find(_.id == productId)
  }

  override def addToCart(productId: ProductId, quantity: Quantity)(using
      ctx: UserCtx[UserRole.Basic],
  ): Unit = {
    val _ = carts.updateWith(ctx.userId) {
      case None =>
        Some(TrieMap(productId -> quantity))
      case Some(userCart) =>
        val _ = userCart.updateWith(productId) {
          case None                   => Some(quantity)
          case Some(originalQuantity) => Some(quantity + originalQuantity)
        }
        Some(userCart)
    }
    ()
  }

  override def getCart()(using ctx: UserCtx[UserRole.Basic]): Cart = {
    carts.get(ctx.userId) match {
      case None       => Cart(Vector.empty)
      case Some(cart) =>
        Cart(cart.map { (productId, quantity) =>
          CartItem(getProductById(productId).get, quantity)
        }.toVector)
    }
  }

  private val products = Vector(
    Product(
      ProductId(idGenerator.generate()),
      ProductName("Tomato"),
      ProductDescription("Juicy tomatoes, yum"),
      ScientificName("Solanum Lycopersicum"),
      Price(1200),
      SaleUnit.Kg,
      ProductCategory.Vegetable,
    ),
    Product(
      ProductId(idGenerator.generate()),
      ProductName("Carrot"),
      ProductDescription("Crispy carrots"),
      ScientificName("Daucus Carota"),
      Price(300),
      SaleUnit.Kg,
      ProductCategory.Vegetable,
    ),
    Product(
      ProductId(idGenerator.generate()),
      ProductName("Spinach"),
      ProductDescription("Healthy spinach"),
      ScientificName("Spinacia Oleracea"),
      Price(1200),
      SaleUnit.Kg,
      ProductCategory.Vegetable,
    ),
    Product(
      ProductId(idGenerator.generate()),
      ProductName("Lettuce"),
      ProductDescription("Crunchy lettuce"),
      ScientificName("Lactuca Sativa"),
      Price(600),
      SaleUnit.Bag250,
      ProductCategory.Vegetable,
    ),
  )

}
