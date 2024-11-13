package org.sarden.core

import java.time.{OffsetDateTime, ZoneId}

trait Clock:
  def currentDateTime(): OffsetDateTime

class LiveClock(zoneId: ZoneId) extends Clock:
  override def currentDateTime(): OffsetDateTime =
    OffsetDateTime.now(zoneId)
