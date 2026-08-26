package gls.usecases

import java.util.concurrent.TimeUnit

import scala.jdk.CollectionConverters.*
import scala.language.experimental.saferExceptions

import io.vertx.core.http.{HttpClientRequest, HttpClientResponse, HttpMethod}
import io.vertx.core.{Future, Handler, Promise, Vertx}
import io.vertx.junit5.VertxTestContext
import org.scalatest.funspec.AnyFunSpec

import gls.*

class AuthUseCase extends AnyFunSpec {

  /* Auth Flow:
    |- (1)  Unauthorized access gets redirect to login page
    |- (2)  Login attempt without user fails
    |- (3)  Signup with invalid email fails
    |- (4)  Signup without matching passwords fails
    |- (5)  Signup with weak password fails
    |- (6)  Signup succeeds with correct data, logs in, redirects to personal home page
    |- (7)  Logout works, personal home page is inaccessible
    |- (8)  Login with invalid email fails
    |- (9)  Login with invalid password fails
    |- (10)  Login succeeds with correct data, redirected to logged in home page
    |- (11) Admin page is not available with Basic user
   */

  // TODO: Extract magic constants
  // TODO: Fix 302 on unauthorized
  describe("Auth flow(FCTMP)") {
    it("Should authenticate, authorize, support login, signup, logout") {
      val port = 9999
      val vertx = Vertx.vertx()
      val testContext = new VertxTestContext()
      val services = Services.create()
      val appConfig = AppConfig("dev", WebConfig(Port.unsafeMake(port)))
      val appRouter = AppRouter(appConfig, vertx, services)
      val sessionCookieValueRegex =
        "vertx-web.session=([^;]+)".r

      for {
        server <- vertx
          .createHttpServer()
          .requestHandler(appRouter.createRouter())
          .listen(port)
        client = vertx.createHttpClient()

        /* Auth Flow (1): Unauthorized access gets redirect to login page */
        unauthorizedAccessResponse <- client
          .request(
            HttpMethod.GET,
            port,
            "localhost",
            "/user/me",
          )
          .send()
        _ <- testContext.assert(
          unauthorizedAccessResponse.statusCode() == 302,
          "No redirect detected(1)",
        )
        _ <- testContext.assert(
          unauthorizedAccessResponse.headers().get("location") == "/user/login",
          "Invalid redirect target(1)",
        )

        /* Auth Flow (2): Login attempt without user fails */
        noUserLoginResponse <- client
          .request(HttpMethod.POST, port, "localhost", "/user/login")
          .formHeader()
          .send("email=na@na&password=12345678")
        _ <- testContext.assert(
          noUserLoginResponse.statusCode() == 400,
          "No error reported(2)",
        )

        /* Auth Flow (3): Signup with invalid email fails */
        invalidEmailSignupResponse <- client
          .request(HttpMethod.POST, port, "localhost", "/user/signup")
          .formHeader()
          .send(
            "email=notok&password=12345678&repeat_password=12345678",
          )
        _ <- testContext.assert(
          invalidEmailSignupResponse.statusCode() == 400,
          "No error reported(3)",
        )

        /* Auth Flow (4): Signup without matching passwords fails */
        passwordMismatchSignupResponse <- client
          .request(HttpMethod.POST, port, "localhost", "/user/signup")
          .formHeader()
          .send(
            "email=ok@ok&password=12345678&repeat_password=87654321",
          )
        _ <- testContext.assert(
          passwordMismatchSignupResponse.statusCode() == 400,
          "No error reported(4)",
        )

        /* Auth Flow (5): Signup with weak password fails */
        weakPasswordSignupResponse <- client
          .request(HttpMethod.POST, port, "localhost", "/user/signup")
          .formHeader()
          .send("email=ok@ok&password=1234&repeat_password=1234")
        _ <- testContext.assert(
          weakPasswordSignupResponse.statusCode() == 400,
          "No error reported(5)",
        )

        /* Auth flow (6):  Signup succeeds with correct data, logs in, redirects to personal home page */
        signupSuccessResponse <- client
          .request(HttpMethod.POST, port, "localhost", "/user/signup")
          .formHeader()
          .send(
            "email=ok@ok&password=12345678&repeat_password=12345678",
          )
        _ <- testContext.assert(
          signupSuccessResponse.statusCode() == 302,
          "Signup failed",
        )
        _ <- testContext.assert(
          signupSuccessResponse.getHeader("location") == "/user/me",
          "Invalid redirect(6)",
        )
        signupSessionCookieValue <- sessionCookieValueRegex
          .findFirstMatchIn(
            Option(signupSuccessResponse.getHeader("set-cookie")).getOrElse(""),
          )
          .flatMap(headerValue => Option(headerValue.group(1))) match {
          case None => testContext.assert[String](false, "No session cookie(6)")
          case Some(cookieValue) => Future.succeededFuture(cookieValue)
        }

        /* Auth flow (7):  Logout works, personal home page is inaccessible */
        logoutResponse <- client
          .request(HttpMethod.GET, port, "localhost", "/user/logout")
          .sessionCookie(signupSessionCookieValue)
          .send()
        _ <- testContext.assert(
          logoutResponse.statusCode() == 302,
          "Logout failed",
        )
        _ <- testContext.assert(
          logoutResponse.getHeader("location") == "/user/login",
          "Invalid redirect(7.1)",
        )

        loggedoutUnauthorizedResponse <- client
          .request(HttpMethod.GET, port, "localhost", "/user/me")
          .sessionCookie(signupSessionCookieValue)
          .send()
        _ <- testContext.assert(
          loggedoutUnauthorizedResponse.statusCode() == 302,
          "Log out was not effective",
        )
        _ <- testContext.assert(
          loggedoutUnauthorizedResponse.getHeader("location") == "/user/login",
          "Invalid redirect(7.2)",
        )

        /* Auth flow (8):  Login with invalid email fails */
        loginInvalidEmailResponse <- client
          .request(HttpMethod.POST, port, "localhost", "/user/login")
          .formHeader()
          .send("email=notok&password=12345678")
        _ <- testContext.assert(
          loginInvalidEmailResponse.statusCode() == 400,
          "No error reported(8)",
        )

        /* Auth flow (9):  Login with invalid password fails */
        loginInvalidPasswordResponse <- client
          .request(HttpMethod.POST, port, "localhost", "/user/login")
          .formHeader()
          .send("email=ok@ok&password=1234")
        _ <- testContext.assert(
          loginInvalidPasswordResponse.statusCode() == 400,
          "No error reported(9)",
        )

        /* Auth flow (10): Login succeeds with correct data, redirected to logged in home page */
        loginValidResponse <- client
          .request(HttpMethod.POST, port, "localhost", "/user/login")
          .formHeader()
          .send("email=ok@ok&password=12345678")
        _ <- testContext.assert(
          loginValidResponse.statusCode() == 302,
          "Login failed",
        )
        _ <- testContext.assert(
          loginValidResponse.getHeader("location") == "/user/me",
          "Invalid redirect(10)",
        )
        loginSessionCookieValue <- sessionCookieValueRegex
          .findFirstMatchIn(
            Option(loginValidResponse.getHeader("set-cookie")).getOrElse(""),
          )
          .flatMap(headerValue => Option(headerValue.group(1))) match {
          case None =>
            testContext.assert[String](false, "No session cookie(10)")
          case Some(cookieValue) => Future.succeededFuture(cookieValue)
        }

        /* Auth flow (11): Admin page is not available with Basic user */
        unauthorizedAdminResponse <- client
          .request(HttpMethod.GET, port, "localhost", "/user/admin")
          .sessionCookie(loginSessionCookieValue)
          .send()
        _ <- testContext.assert(
          unauthorizedAdminResponse.statusCode() == 302,
          "Admin is not authorized",
        )
        _ <- testContext.assert(
          unauthorizedAdminResponse.getHeader("location") == "/user/me",
          "Invalid redirect(11)",
        )

        /* Cleanup */
        _ <- server.shutdown()
        _ <- testContext.complete()
      } yield ()

      assert(testContext.awaitCompletion(5, TimeUnit.SECONDS))

      if (testContext.failed()) {
        throw testContext.causeOfFailure()
      }

    }
  }

  extension (testContext: VertxTestContext)
    def assert[A](condition: Boolean, message: String): Future[A] = {
      if (!condition) {
        testContext.failNow(message)
      }
      Future.succeededFuture()
    }
    def complete(): Future[Unit] = {
      testContext.completeNow()
      Future.succeededFuture()
    }

  extension (requestBuilder: Future[HttpClientRequest])
    def formHeader(): Future[HttpClientRequest] = {
      requestBuilder.compose { request =>
        request.putHeader(
          "Content-Type",
          "application/x-www-form-urlencoded",
        )
        Future.succeededFuture(request)
      }
    }
    def sessionCookie(value: String): Future[HttpClientRequest] = {
      requestBuilder.compose { request =>
        request.putHeader(
          "Cookie",
          s"vertx-web.session=${value}",
        )
        Future.succeededFuture(request)
      }
    }
    def send(payload: String | Null = null): Future[HttpClientResponse] = {
      requestBuilder.compose { request =>
        payload match {
          case body: String => request.send(body)
          case _            => request.send()
        }
      }
    }

}
