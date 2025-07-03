package org.sarden.core

import io.scalaland.chimney.dsl.*
import neotype.*
import zio.*

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

  case class DataTransformationError(errors: Result.Errors)
      extends InternalError(
        s"Data transformation failed with error: ${errors.asErrorPathMessages.mkString(",")}",
      )

  extension [From](from: From)
    def transformIntoPartialZIO[To](using
        transformer: PartialTransformer.AutoDerived[From, To],
    ): IO[DataTransformationError, To] =
      ZIO.fromEither:
        from
          .transformIntoPartial[To]
          .asEither
          .left
          .map(DataTransformationError.apply)

    def transformIntoPartialZIOOrDie[To](using
        transformer: PartialTransformer.AutoDerived[From, To],
    ): UIO[To] =
      ZIO.fromEither {
        from
          .transformIntoPartial[To]
          .asEither
          .left
          .map(DataTransformationError.apply)
      }.orDie
