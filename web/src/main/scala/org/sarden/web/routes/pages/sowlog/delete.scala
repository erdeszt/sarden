package org.sarden.web.routes.pages.sowlog

import org.sarden.InvalidRequestError
import sttp.model.{HeaderNames, StatusCode}
import sttp.tapir.ztapir.*
import zio.*
import org.sarden.bindings.mapping.given
import org.sarden.domain.sowlog.{SowlogEntryId, SowlogService}
import org.sarden.web.*
import org.sarden.web.routes.pages.*

case class InvalidSowlogEntryIdInputError(raw: String)
    extends InvalidRequestError(s"Invalid sow log entry id format: ${raw}")

val deleteSowlogEntry: AppServerEndpoint = baseEndpoint.post
  .in("sowlog" / path[String]("id") / "delete")
  .out(statusCode(StatusCode.Found).and(header[String](HeaderNames.Location)))
  .zServerLogic: rawId =>
    for
      id <- ZIO
        .fromEither(SowlogEntryId.fromString(rawId))
        .orElseFail(InvalidSowlogEntryIdInputError(rawId))
        .orDie
      _ <- ZIO.serviceWithZIO[SowlogService]:
        _.deleteEntry(id)
    yield "/sowlog"
