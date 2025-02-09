package org.sarden.core

import doobie.util.{Read, Write}
import io.scalaland.chimney.partial.Result
import io.scalaland.chimney.{PartialTransformer, Transformer}
import neotype.Newtype

object ulid:

  export com.github.f4b6a3.ulid.Ulid

  trait UlidNewtype extends Newtype[Ulid]:
    self =>
    given PartialTransformer[String, self.Type] = PartialTransformer: raw =>
      Result.fromEitherString:
        // NOTE: This is safe because self.Type =:= Ulid
        try Right(com.github.f4b6a3.ulid.Ulid.from(raw).asInstanceOf[self.Type])
        catch
          case error: Exception =>
            Left(error.getMessage)

    given Transformer[self.Type, String] = (ulid: self.Type) => ulid.toString

  given Read[Ulid] = Read[String].map(com.github.f4b6a3.ulid.Ulid.from)
  given Write[Ulid] = Write[String].contramap(_.toString)
