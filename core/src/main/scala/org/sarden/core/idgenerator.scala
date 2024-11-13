package org.sarden.core

import com.github.f4b6a3.ulid.{Ulid, UlidCreator}

trait IdGenerator:
  def next(): Ulid

class LiveIdGenerator extends IdGenerator:
  override def next(): Ulid =
    UlidCreator.getMonotonicUlid()
