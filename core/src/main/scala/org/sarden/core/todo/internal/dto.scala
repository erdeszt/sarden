package org.sarden.core.todo.internal

import doobie.Read
import io.scalaland.chimney.partial.Result
import io.scalaland.chimney.{PartialTransformer, Transformer}
import zio.json.*

import org.sarden.core.todo.TodoSchedule

private[internal] case class TodoDTO(
    id: String,
    name: String,
    schedule: String,
    notifyBefore: String,
    lastRun: Option[Long],
    createdAt: Long,
) derives Read

given JsonCodec[TodoSchedule] = JsonCodec.derived

given PartialTransformer[String, TodoSchedule] = PartialTransformer: raw =>
  Result.fromEitherString(raw.fromJson)

given Transformer[TodoSchedule, String] = _.toJson
