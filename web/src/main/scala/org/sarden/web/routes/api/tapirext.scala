package org.sarden.web.routes.api

import io.scalaland.chimney.PartialTransformer
import io.scalaland.chimney.dsl.*
import sttp.tapir.ztapir.*
import zio.*

import org.sarden.core.SystemErrors.DataInconsistencyError

val baseEndpoint = sttp.tapir.ztapir.endpoint.in("api")

extension [From](from: From)
  def transformIntoPartialZIO[To](using
      transformer: PartialTransformer.AutoDerived[From, To],
  ): IO[DataInconsistencyError, To] =
    ZIO.fromEither:
      from.transformIntoPartial[To].asEither.left.map(DataInconsistencyError(_))

  def transformIntoPartialZIOOrDie[To](using
      transformer: PartialTransformer.AutoDerived[From, To],
  ): UIO[To] =
    ZIO.fromEither {
      from
        .transformIntoPartial[To]
        .asEither
        .left
        .map(DataInconsistencyError(_))
    }.orDie
