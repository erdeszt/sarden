package gls

import gls.domain.user.*

case class Services(
    user: UserService,
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

    Services(userService)
  }

}
