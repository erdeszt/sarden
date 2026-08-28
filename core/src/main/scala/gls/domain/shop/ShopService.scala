package gls.domain.shop

import com.github.f4b6a3.ulid.Ulid

import gls.{Clock, IdGenerator}

trait ShopService {}

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
) extends ShopService {}
