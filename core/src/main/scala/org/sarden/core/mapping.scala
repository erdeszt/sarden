package org.sarden.core

import io.scalaland.chimney.dsl.*
import neotype.*
import zio.*

import org.sarden.core.SystemErrors.DataInconsistencyError

object mapping:

  export io.scalaland.chimney.partial.Result
  export io.scalaland.chimney.{PartialTransformer, Transformer}
  export neotype.interop.doobie.newtypeGet
  export neotype.interop.doobie.newtypePut

  export org.sarden.core.time.given
  export org.sarden.core.ulid.given

  given [A, B](using newType: Newtype.WithType[A, B]): Transformer[A, B] =
    newType.unsafeMake(_)

  given [A, B](using newType: Newtype.WithType[A, B]): Transformer[B, A] =
    newType.unwrap(_)

  extension [From](from: From)
    def transformIntoPartialZIO[To](using
        transformer: PartialTransformer.AutoDerived[From, To],
    ): IO[DataInconsistencyError, To] =
      ZIO.fromEither:
        from
          .transformIntoPartial[To]
          .asEither
          .left
          .map(DataInconsistencyError(_))

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
