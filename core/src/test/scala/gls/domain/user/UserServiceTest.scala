package gls.domain.user

import scala.language.experimental.saferExceptions

import org.scalatest.funspec.AnyFunSpec

import gls.*
import gls.Util.{expect, yolo}
import gls.domain.user.*

class UserServiceTest extends AnyFunSpec {

  def createUserService(): UserService = {
    LiveUserService(
      UserRepo.inMemory(),
      UlidWrapperIdGenerator(UserId(_)),
      JavaTimeClock(),
      IdentityPasswordHasher(),
    )
  }

  describe("UserService") {

    describe("Login") {

      it("should login after creating a user") {
        val service = createUserService()
        val email = Email("ok@ok")
        val password = PlainPassword("validpassword")
        val user = yolo(service.createUser(email, password))

        val loggedInUser = service.getByCredentials(email, password)

        val _ = assert(loggedInUser.isDefined)
        val _ = assert(loggedInUser.exists(_.email == user.email))
        assert(loggedInUser.exists(_.password == user.password))
      }

      it("should prevent login for non existing accounts") {
        val service = createUserService()
        val user = service.getByCredentials(
          Email("non@existing.email"),
          PlainPassword("irrelevant"),
        )

        assert(user.isEmpty)
      }

      it("should prevent login with invalid password") {
        val service = createUserService()
        val user = yolo(
          service.createUser(
            Email("test@test.test"),
            PlainPassword("validpassword"),
          ),
        )

        val loggedInUser =
          service.getByCredentials(user.email, PlainPassword("invalidpassword"))

        assert(loggedInUser.isEmpty)
      }

    }

    describe("Signup") {
      it("should validate email") {
        val service = createUserService()

        expect[EmailFormatError] {
          service.createUser(Email("no_at"), PlainPassword("validpassword"))
        }
      }

      it("should validate password") {
        val service = createUserService()

        expect[WeakPasswordError] {
          service.createUser(Email("test@test.test"), PlainPassword("short"))
        }
      }

    }

  }

}
