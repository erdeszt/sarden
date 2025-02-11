package org.sarden.web

import zio.*

case class WebConfig(
    serverMode: ServerMode,
    apiAuthConfig: ApiAuthConfig,
    siteAuthConfig: SiteAuthConfig,
)

case class ApiAuthConfig(apiKey: String)
case class SiteAuthConfig()

enum ServerMode derives CanEqual:
  case OnlyApi
  case OnlySite
  case Both

object WebConfig:
  val live: ULayer[WebConfig] =
    ZLayer.succeed(
      WebConfig(ServerMode.Both, ApiAuthConfig("dev"), SiteAuthConfig()),
    )
