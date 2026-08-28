package gls.domain.shop

import com.github.f4b6a3.ulid.Ulid

import gls.{Clock, IdGenerator}

case class ProductList(items: Vector[Product], statistics: ProductStatistics)

trait ShopService {
  def listProducts(query: ProductQuery): ProductList
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

  override def listProducts(query: ProductQuery): ProductList = {
    val products = Vector(
      Product(
        ProductId(idGenerator.generate()),
        ProductName("Tomato"),
        ScientificName("Solanum Lycopersicum"),
        Price(1200),
        SaleUnit.Kg,
        ProductCategory.Vegetable,
      ),
      Product(
        ProductId(idGenerator.generate()),
        ProductName("Carrot"),
        ScientificName("Daucus Carota"),
        Price(300),
        SaleUnit.Kg,
        ProductCategory.Vegetable,
      ),
      Product(
        ProductId(idGenerator.generate()),
        ProductName("Spinach"),
        ScientificName("Spinacia Oleracea"),
        Price(1200),
        SaleUnit.Kg,
        ProductCategory.Vegetable,
      ),
      Product(
        ProductId(idGenerator.generate()),
        ProductName("Lettuce"),
        ScientificName("Lactuca Sativa"),
        Price(600),
        SaleUnit.Bag250,
        ProductCategory.Vegetable,
      ),
    )
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

}
