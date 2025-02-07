package org.sarden.core.sowlog

import io.github.gaelrenoux.tranzactio.doobie.*
import zio.*

import org.sarden.core.sowlog.internal.{LiveSowlogRepo, SowlogRepo}

trait SowlogService

object SowlogService:
  val live: URLayer[Database, SowlogService] =
    ZLayer.fromFunction(LiveSowlogService(LiveSowlogRepo(), _))

class LiveSowlogService(repo: SowlogRepo, db: Database) extends SowlogService
