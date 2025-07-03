package org.sarden

import zio.*

import java.time.ZoneId

case class CoreConfig(
    zoneId: ZoneId,
    dbUrl: String,
)

object CoreConfig:
  val live: ULayer[CoreConfig] =
    ZLayer.succeed(CoreConfig(ZoneId.of("UTC"), "jdbc:sqlite:dev.db"))
