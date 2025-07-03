package org.sarden.web.routes.api

import com.github.f4b6a3.ulid.UlidCreator
import org.sarden.CoreServices
import org.sarden.bindings.Password
import sttp.tapir.ztapir.*
import zio.ZIO
import org.sarden.domain.user.{AuthenticationFailedError, User, UserId, UserName}
import org.sarden.web.ApiAuthConfig

def baseEndpoint(using
    apiAuthConfig: ApiAuthConfig,
): ZPartialServerEndpoint[
  CoreServices,
  String,
  User,
  Unit,
  AuthenticationFailedError,
  Unit,
  Any,
] =
  sttp.tapir.ztapir.endpoint
    .in("api")
    .securityIn(auth.apiKey(header[String]("X-Api-Key")))
    .errorOut(stringBody)
    .mapErrorOutDecode(_ =>
      throw RuntimeException(
        "Mapping of Strings to error types is not supported",
      ),
    )((error: AuthenticationFailedError) => error.getMessage)
    .zServerSecurityLogic: apiKey =>
      if apiKey == apiAuthConfig.apiKey then
        ZIO.succeed(
          User(
            UserId(UlidCreator.getMonotonicUlid()),
            UserName(apiKey),
            Password("gibberish"),
          ),
        )
      else ZIO.fail(AuthenticationFailedError())
