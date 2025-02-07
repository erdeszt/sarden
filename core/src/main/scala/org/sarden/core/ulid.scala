package org.sarden.core

import com.github.f4b6a3.ulid.Ulid
import io.scalaland.chimney.PartialTransformer
import io.scalaland.chimney.partial.Result
import neotype.Newtype

trait UlidNewtype extends Newtype[Ulid]:
  self =>
  given PartialTransformer[String, self.Type] = PartialTransformer: raw =>
    Result.fromEitherString:
      // NOTE: This is safe because self.Type =:= Ulid
      try Right(Ulid.from(raw).asInstanceOf[self.Type])
      catch
        case e: Exception =>
          Left(e.getMessage)
