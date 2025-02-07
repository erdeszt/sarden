package org.sarden.core.domain.sowlog

import io.github.gaelrenoux.tranzactio.doobie.*
import zio.*

import org.sarden.core.domain.sowlog.internal.*

trait SowlogService

object SowlogService:
  val live: URLayer[Database, SowlogService] =
    ZLayer.fromFunction(LiveSowlogService(LiveSowlogRepo(), _))

class LiveSowlogService(repo: SowlogRepo, db: Database) extends SowlogService
