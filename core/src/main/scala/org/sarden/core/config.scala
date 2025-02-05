package org.sarden.core

import java.time.ZoneId

import zio.*

case class CoreConfig(
    zoneId: ZoneId,
    dbUrl: String,
)

object CoreConfig:
  val live: ULayer[CoreConfig] =
    ZLayer.succeed(CoreConfig(ZoneId.of("UTC"), "jdbc:sqlite:dev.db"))
