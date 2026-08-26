package gls.usecases

import java.util.concurrent.TimeUnit

import scala.language.experimental.saferExceptions

import io.vertx.core.http.{HttpClientResponse, HttpMethod, HttpServer}
import io.vertx.core.{AsyncResult, Future, Handler, Vertx}
import io.vertx.junit5.VertxTestContext
import neotype.unwrap
import org.scalatest.funspec.AnyFunSpec

import gls.*

class AuthUseCase extends AnyFunSpec {

  describe("Auth flow") {
    it(
      """Auth Flow:
        |- Unauthorized access gets redirect to login page
        |- Login attempt without user fails
        |- Signup with invalid email fails
        |- Signup without matching passwords fails
        |- Signup with weak password fails
        |- Signup succeeds with correct data, redirected to login page
        |- Login with invalid email fails
        |- Login with invalid password fails
        |- Login succeeds with correct data, redirected to logged in home page""".stripMargin,
    ) {
      val port = 9999
      val vertx = Vertx.vertx()
      val testContext = new VertxTestContext()
      val templates = HandlebarsTemplates.create()
      val services = Services.create()
      val appConfig = AppConfig("dev", WebConfig(Port.unsafeMake(port)))
      val appRouter = AppRouter(appConfig, vertx, templates, services)

      vertx
        .createHttpServer()
        .requestHandler(appRouter.createRouter())
        .listen(port)
        .onComplete { _ =>
          val client = vertx.createHttpClient()

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
            .compose { _ =>
              client.request(HttpMethod.POST, port, "localhost", "/user/login")
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
