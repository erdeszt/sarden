package gls.domain.shop

import com.github.f4b6a3.ulid.Ulid
import gls.domain.*

opaque type ProductId = Ulid
object ProductId extends Newtype[ProductId, Ulid]

opaque type ProductName = String
object ProductName extends Newtype[ProductName, String]

// TODO: Binomial name
opaque type ScientificName = String
object ScientificName extends Newtype[ScientificName, String]

// TODO: Non zero prices only(refined)
opaque type Price = Int
object Price extends Newtype[Price, Int]

opaque type ProductImageUrl = String
object ProductImageUrl extends Newtype[ProductImageUrl, String]

opaque type ProductDescription = String
object ProductDescription extends Newtype[ProductDescription, String]

// TODO: Positive values only(refined)
opaque type Quantity = Int
object Quantity extends Newtype[Quantity, Int] {
  extension (quantity: Quantity)
    def +(other: Quantity): Quantity = {
      Quantity(quantity.unwrap + other.unwrap)
    }
}

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
    description: ProductDescription,
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

case class CartItem(
    product: Product,
    quantity: Quantity,
)

case class Cart(
    items: Vector[CartItem],
)
