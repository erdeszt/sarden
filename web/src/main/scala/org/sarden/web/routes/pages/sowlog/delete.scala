package org.sarden.web.routes.pages.sowlog

import io.scalaland.chimney.dsl.*
import sttp.model.{HeaderNames, StatusCode}
import sttp.tapir.ztapir.*
import zio.*

import org.sarden.core.InvalidRequestError
import org.sarden.core.mapping.given
import org.sarden.core.sowlog.*
import org.sarden.web.*
import org.sarden.web.routes.pages.*

case class InvalidSowlogEntryIdInputError(raw: String)
    extends InvalidRequestError(s"Invalid sow log entry id format: ${raw}")

val deleteSowlogEntry: AppServerEndpoint = baseEndpoint.post
  .in("sowlog" / "delete" / path[String]("id"))
  .out(statusCode(StatusCode.Found).and(header[String](HeaderNames.Location)))
  .zServerLogic: rawId =>
    for
      id <- ZIO
        .fromEither {
          rawId
            .transformIntoPartial[SowlogEntryId]
            .asEither
        }
        .orElseFail(InvalidSowlogEntryIdInputError(rawId))
        .orDie
      _ <- ZIO.serviceWithZIO[SowlogService]:
        _.deleteEntry(id)
    yield "/sowlog"
