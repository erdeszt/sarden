package gls.domain.user

import gls.domain.user.{Email, LiveUserService, UserId, UserRepo}
import gls.{IdentityPasswordHasher, JavaTimeClock, PlainPassword, UlidWrapperIdGenerator}
import org.scalatest.funspec.AnyFunSpec

class UserServiceTest extends AnyFunSpec {
  val userService = LiveUserService(
    UserRepo.inMemory(),
    UlidWrapperIdGenerator(UserId(_)),
    JavaTimeClock(),
    IdentityPasswordHasher(),
  )

  describe("UserService") {
    it("should login after creating a user") {
      val password = PlainPassword("testpwd")
      val user = userService.createUser(Email("test@test.test"), password)

      val loggedInUser = userService.login(user.email, password)

      assert(loggedInUser.isDefined)
      assert(loggedInUser.exists(_.email == user.email))
      assert(loggedInUser.exists(_.password == user.password))
    }

    it("should prevent login for non existing accounts") {
      val user = userService.login(
        Email("non@existing.email"),
        PlainPassword("irrelevant"),
      )

      assert(user.isEmpty)
    }

    it("should prevent login with invalid password") {
      val user = userService.createUser(
        Email("test@test.test"),
        PlainPassword("correct"),
      )

      val loggedInUser =
        userService.login(user.email, PlainPassword("incorrect"))

      assert(loggedInUser.isEmpty)
    }
  }

}
