package gls.usecases

import java.util.concurrent.TimeUnit

import scala.jdk.CollectionConverters.*
import scala.language.experimental.saferExceptions

import io.vertx.core.http.HttpMethod
import io.vertx.core.{Future, Handler, Vertx}
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
    |- (6)  Signup succeeds with correct data, redirected to login page
    |- (7)  Login with invalid email fails
    |- (8)  Login with invalid password fails
    |- (9)  Login succeeds with correct data, redirected to logged in home page
    |- (10) Admin page is not available with Basic user
    |- (11) After logout personal home page is not available
   */

  // TODO: Extract magic constants
  // TODO: Fix 302 on unauthorized
  describe("Auth flow") {
    it("Should authenticate, authorize, support login, signup, logout") {
      val port = 9999
      val vertx = Vertx.vertx()
      val testContext = new VertxTestContext()
      val services = Services.create()
      val appConfig = AppConfig("dev", WebConfig(Port.unsafeMake(port)))
      val appRouter = AppRouter(appConfig, vertx, services)

      vertx
        .createHttpServer()
        .requestHandler(appRouter.createRouter())
        .listen(port)
        .onComplete { _ =>
          val client = vertx.createHttpClient()

          Future
            .succeededFuture()
            .compose { _ =>
              /* Auth Flow (1): Unauthorized access => redirect */
              client
                .request(
                  HttpMethod.GET,
                  port,
                  "localhost",
                  "/user/me",
                )
                .compose(_.send())
                .compose { response =>
                  if (response.statusCode() != 302) {
                    testContext.failNow("No redirect detected")
                  }
                  if (response.headers().get("location") != "/user/login") {
                    testContext.failNow("Invalid redirect target")
                  }
                  Future.succeededFuture()
                }
            }
            .compose { _ =>
              /* Auth Flow (2): No user => login fails */
              client
                .request(HttpMethod.POST, port, "localhost", "/user/login")
                .compose { request =>
                  request.putHeader(
                    "Content-Type",
                    "application/x-www-form-urlencoded",
                  )
                  Future.succeededFuture(request)
                }
                .compose(_.send("email=na@na&password=12345678"))
                .compose { response =>
                  if (response.statusCode() != 400) {
                    testContext.failNow("No error reported")
                  }
                  Future.succeededFuture()
                }
            }
            .compose { _ =>
              /* Auth Flow (3): Invalid email => signup fails */
              client
                .request(HttpMethod.POST, port, "localhost", "/user/signup")
                .compose { request =>
                  request.putHeader(
                    "Content-Type",
                    "application/x-www-form-urlencoded",
                  )
                  Future.succeededFuture(request)
                }
                .compose(
                  _.send(
                    "email=notok&password=12345678&repeat_password=12345678",
                  ),
                )
                .compose { response =>
                  if (response.statusCode() != 400) {
                    testContext.failNow("No error reported")
                  }
                  Future.succeededFuture()
                }
            }
            .compose { _ =>
              /* Auth Flow (4): Passwords don't match => signup fails */
              client
                .request(HttpMethod.POST, port, "localhost", "/user/signup")
                .compose { request =>
                  request.putHeader(
                    "Content-Type",
                    "application/x-www-form-urlencoded",
                  )
                  Future.succeededFuture(request)
                }
                .compose(
                  _.send(
                    "email=ok@ok&password=12345678&repeat_password=87654321",
                  ),
                )
                .compose { response =>
                  if (response.statusCode() != 400) {
                    testContext.failNow("No error reported")
                  }
                  Future.succeededFuture()
                }
            }
            .compose { _ =>
              /* Auth Flow (5): Weak password => signup fails */
              client
                .request(HttpMethod.POST, port, "localhost", "/user/signup")
                .compose { request =>
                  request.putHeader(
                    "Content-Type",
                    "application/x-www-form-urlencoded",
                  )
                  Future.succeededFuture(request)
                }
                .compose(
                  _.send("email=ok@ok&password=1234&repeat_password=1234"),
                )
                .compose { response =>
                  if (response.statusCode() != 400) {
                    testContext.failNow("No error reported")
                  }
                  Future.succeededFuture()
                }
            }
            .compose { _ =>
              /* Auth Flow (6): Signup succeeds */
              client
                .request(HttpMethod.POST, port, "localhost", "/user/signup")
                .compose { request =>
                  request.putHeader(
                    "Content-Type",
                    "application/x-www-form-urlencoded",
                  )
                  Future.succeededFuture(request)
                }
                .compose(
                  _.send(
                    "email=ok@ok&password=12345678&repeat_password=12345678",
                  ),
                )
                .compose { response =>
                  if (response.statusCode() != 302) {
                    testContext.failNow("Signup failed")
                  }
                  if (response.getHeader("location") != "/user/login") {
                    testContext.failNow("Invalid redirect")
                  }

                  Future.succeededFuture()
                }
            }
            .compose { _ =>
              /* Auth Flow (7): Invalid email => login fails */
              client
                .request(HttpMethod.POST, port, "localhost", "/user/login")
                .compose { request =>
                  request.putHeader(
                    "Content-Type",
                    "application/x-www-form-urlencoded",
                  )
                  Future.succeededFuture(request)
                }
                .compose(_.send("email=na@na&password=12345678"))
                .compose { response =>
                  if (response.statusCode() != 400) {
                    testContext.failNow("No error reported")
                  }

                  Future.succeededFuture()
                }
            }
            .compose { _ =>
              /* Auth Flow (8): Invalid password => login fails */
              client
                .request(HttpMethod.POST, port, "localhost", "/user/login")
                .compose { request =>
                  request.putHeader(
                    "Content-Type",
                    "application/x-www-form-urlencoded",
                  )
                  Future.succeededFuture(request)
                }
                .compose(_.send("email=ok@ok&password=1234"))
                .compose { response =>
                  if (response.statusCode() != 400) {
                    testContext.failNow("No error reported")
                  }

                  Future.succeededFuture()
                }
            }
            .compose { _ =>
              /* Auth Flow (9): Login succeeds */
              client
                .request(HttpMethod.POST, port, "localhost", "/user/login")
                .compose { request =>
                  request.putHeader(
                    "Content-Type",
                    "application/x-www-form-urlencoded",
                  )
                  Future.succeededFuture(request)
                }
                .compose(_.send("email=ok@ok&password=12345678"))
                .compose { response =>
                  if (response.statusCode() != 302) {
                    testContext.failNow("Login failed")
                  }
                  if (response.getHeader("location") != "/user/me") {
                    testContext.failNow("Invalid redirect")
                  }

                  val sessionCookieValueRegex =
                    "vertx-web.session=([^;]+)".r

                  val cookies = response
                    .cookies()
                    .asScala
                    .mkString("")

                  val sessionCookieValue = sessionCookieValueRegex
                    .findFirstMatchIn(cookies)
                    .get
                    .group(1)

                  Future.succeededFuture(sessionCookieValue)
                }
            }
            .compose { sessionCookieValue =>
              /* Auth Flow (10): Admin is authorized */
              client
                .request(HttpMethod.GET, port, "localhost", "/user/admin")
                .compose { request =>
                  request.putHeader(
                    "Cookie",
                    s"vertx-web.session=${sessionCookieValue}",
                  )
                  Future.succeededFuture(request)
                }
                .compose(_.send())
                .compose { response =>
                  if (response.statusCode() != 302) {
                    testContext.failNow("Admin is not authorized")
                  }
                  if (response.getHeader("location") != "/user/me") {
                    testContext.failNow("Invalid redirect")
                  }

                  Future.succeededFuture(sessionCookieValue)
                }
            }
            .compose { sessionCookieValue =>
              /* Auth Flow (11): Logout flow */
              client
                .request(HttpMethod.GET, port, "localhost", "/user/logout")
                .compose { request =>
                  request.putHeader(
                    "Cookie",
                    s"vertx-web.session=${sessionCookieValue}",
                  )
                  Future.succeededFuture(request)
                }
                .compose(_.send())
                .compose { response =>
                  if (response.statusCode() != 302) {
                    testContext.failNow("Logout failed")
                  }
                  if (response.getHeader("location") != "/user/login") {
                    testContext.failNow("Invalid redirect")
                  }

                  Future.succeededFuture()
                }
                .compose { _ =>
                  client
                    .request(HttpMethod.GET, port, "localhost", "/user/me")
                    .compose { request =>
                      request.putHeader(
                        "Cookie",
                        s"vertx-web.session=${sessionCookieValue}",
                      )
                      Future.succeededFuture(request)
                    }
                    .compose(_.send())
                    .compose { response =>
                      if (response.statusCode() != 302) {
                        testContext.failNow("Logout failed")
                      }
                      if (response.getHeader("location") != "/user/login") {
                        testContext.failNow("Invalid redirect")
                      }

                      Future.succeededFuture()
                    }
                }
            }
            .onComplete { _ => testContext.completeNow() }
        }

      assert(testContext.awaitCompletion(5, TimeUnit.MINUTES))

      if (testContext.failed()) {
        throw testContext.causeOfFailure()
      }

    }
  }

}
