package org.sarden.web

import zio.*

case class WebConfig(
    apiAuthConfig: ApiAuthConfig,
    siteAuthConfig: SiteAuthConfig,
)

case class ApiAuthConfig()
case class SiteAuthConfig()

object WebConfig:
  val live: ULayer[WebConfig] =
    ZLayer.succeed(WebConfig(ApiAuthConfig(), SiteAuthConfig()))
