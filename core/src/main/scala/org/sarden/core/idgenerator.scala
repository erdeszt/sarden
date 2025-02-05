package org.sarden.core

import com.github.f4b6a3.ulid.{Ulid, UlidCreator}
import zio.*

trait IdGenerator:
  def next(): UIO[Ulid]

object IdGenerator:
  val live: ULayer[IdGenerator] = ZLayer.succeed(LiveIdGenerator())

class LiveIdGenerator extends IdGenerator:
  override def next(): UIO[Ulid] =
    ZIO.attempt(UlidCreator.getMonotonicUlid()).orDie
