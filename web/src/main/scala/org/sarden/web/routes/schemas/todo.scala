package org.sarden.web.routes.schemas

import scala.concurrent.duration.FiniteDuration

import sttp.tapir.Schema

import org.sarden.core.domain.todo.*

object todo:
  given Schema[TodoSchedule] = Schema.derived
  given Schema[FiniteDuration] = Schema.anyObject
  given Schema[TodoId] = Schema.string
  given Schema[TodoName] = Schema.string
  given Schema[CreateTodo] = Schema.derived
  given Schema[Todo] = Schema.derived
