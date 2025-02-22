package org.sarden.web.routes.pages.plants

import io.scalaland.chimney.dsl.*
import sttp.model.{HeaderNames, StatusCode}
import sttp.tapir.ztapir.*
import zio.*

import org.sarden.core.mapping.given
import org.sarden.core.plant.*
import org.sarden.web.*
import org.sarden.web.routes.pages.*

val deletePlant: AppServerEndpoint = baseEndpoint.post
  .in("plants" / path[String]("id") / "delete")
  .out(statusCode(StatusCode.Found).and(header[String](HeaderNames.Location)))
  .zServerLogic: rawId =>
    for
      id <- ZIO
        .fromEither {
          rawId
            .transformIntoPartial[PlantId]
            .asEither
        }
        .orElseFail(InvalidPlantIdInputError(rawId))
        .orDie
      _ <- ZIO.serviceWithZIO[PlantService]:
        _.deletePlant(id).orDie
    yield "/plants"
