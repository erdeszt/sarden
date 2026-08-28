package gls.domain.shop

import com.github.f4b6a3.ulid.Ulid
import neotype.*

type ProductId = ProductId.Type
object ProductId extends Newtype[Ulid]

case class Product(id: ProductId)

trait ProductQuery
