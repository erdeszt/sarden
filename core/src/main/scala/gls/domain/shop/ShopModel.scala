package gls.domain.shop

import com.github.f4b6a3.ulid.Ulid
import neotype.*

type ProductId = ProductId.Type
object ProductId extends Newtype[Ulid]

type ProductName = ProductName.Type
object ProductName extends Newtype[String]

type ScientificName = ScientificName.Type
object ScientificName extends Newtype[String]

type Price = Price.Type
object Price extends Newtype[Int]

type ProductImageUrl = ProductImageUrl.Type
object ProductImageUrl extends Newtype[String]

enum SaleUnit {
  case Kg
  case Bag250
  case Bag500
  case Bunch
}

enum ProductCategory {
  case Fruit
  case Vegetable
  case Herb
  case Plant
}

object ProductCategory {
  val all: Vector[ProductCategory] = Vector(Fruit, Vegetable, Herb, Plant)
}

case class Product(
    id: ProductId,
    name: ProductName,
    scientificName: ScientificName,
    price: Price,
    saleUnit: SaleUnit,
    category: ProductCategory,
)

case class ProductStatistics(
    totalByCategory: Map[ProductCategory, Int],
)

trait ProductQuery

object ProductQuery {
  def empty(): ProductQuery = {
    new ProductQuery {}
  }
}
