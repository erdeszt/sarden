package org.sarden.core

import doobie.util.fragment.Fragment
import doobie.util.update
import doobie.{Query0, Read}
import io.github.gaelrenoux.tranzactio.doobie.*
import io.github.gaelrenoux.tranzactio.{ErrorStrategies, ErrorStrategiesRef}
import io.scalaland.chimney.PartialTransformer
import io.scalaland.chimney.dsl.*
import io.scalaland.chimney.partial.Result
import zio.*

import org.sarden.core.mapping.DataTransformationError

object tx:
  type Tx = Connection

  extension (runner: Tx.Runner)
    def runOrDie[R, E, A](
        zio: => ZIO[Connection & R, E, A],
        commitOnFailure: => Boolean = false,
    )(implicit
        errorStrategies: ErrorStrategiesRef = ErrorStrategies.Parent,
        trace: Trace,
    ): ZIO[R, E, A] =
      runner.transactionOrDie(zio, commitOnFailure)

  object Tx:

    type Runner = Database
    val Bulk: update.Update.type = doobie.Update

    def apply[A](query: => Query[A])(implicit
        trace: Trace,
    ): URIO[Tx, A] =
      tzio(query).orDie

    def runOrDie[R, E, A](
        zio: => ZIO[Connection & R, E, A],
        commitOnFailure: => Boolean = false,
    )(implicit
        errorStrategies: ErrorStrategiesRef = ErrorStrategies.Parent,
        trace: Trace,
    ): ZIO[R & Database, E, A] =
      Database.transactionOrDie(zio, commitOnFailure)

  extension (fragment: Fragment)
    def queryThrough[DTO, DO](using
        read: Read[DTO],
        partialTransformer: PartialTransformer.AutoDerived[DTO, DO],
    ): Query0[DO] =
      fragment.query[DO](using
        read.map: dto =>
          dto.transformIntoPartial[DO].asEither match
            case Left(error) =>
              throw DataTransformationError(error)
            case Right(value) => value,
      )

    def queryTransform[DTO: Read]: QueryTransformer[DTO] =
      QueryTransformer(fragment)

    def queryTransformPartial[DTO: Read]: QueryTransformerPartial[DTO] =
      QueryTransformerPartial(fragment)

  class QueryTransformer[DTO](fragment: Fragment):
    def apply[DO](
        f: DTO => DO,
    )(using read: Read[DTO]): Query0[DO] =
      fragment.query[DO](using read.map(f))

  class QueryTransformerPartial[DTO](fragment: Fragment):
    def apply[DO](
        f: DTO => Result[DO],
    )(using read: Read[DTO]): Query0[DO] =
      fragment.query[DO](using
        read.map: dto =>
          f(dto).asEither match
            case Left(error) =>
              throw DataTransformationError(error)
            case Right(value) => value,
      )

  export doobie.syntax.string.*
