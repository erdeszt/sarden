package org.sarden.core

import java.time.ZoneId

case class CoreConfig(
    zoneId: ZoneId,
    dbUrl: String,
)
