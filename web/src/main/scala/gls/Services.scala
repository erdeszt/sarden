package gls

import gls.domain.shop.ShopService
import gls.domain.user.*

case class Services(
    user: UserService,
    shop: ShopService,
)

object Services {

  def create(): Services = {
    val clock = JavaTimeClock()
    val passwordHasher = BCryptPasswordHasher()

    val userService = UserService.live(
      UlidWrapperIdGenerator(UserId(_)),
      clock,
      passwordHasher,
    )

    val shopService = ShopService.live(
      UlidIdGenerator,
      clock,
    )

    Services(userService, shopService)
  }

}
