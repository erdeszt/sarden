package org.sarden.core

import doobie.util.update
import io.github.gaelrenoux.tranzactio.doobie.*
import io.github.gaelrenoux.tranzactio.{ErrorStrategies, ErrorStrategiesRef}
import zio.*

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

  export doobie.syntax.string.*
