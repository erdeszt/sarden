package org.sarden.core

import scala.util.control.NoStackTrace

import io.scalaland.chimney.partial.Result

// TODO: Nice error types with messages/causes
sealed abstract class AppError(message: String, cause: Option[Throwable])
    extends Exception:
  override def getCause: Throwable | Null = cause.orNull
  override def getMessage: String = message
abstract class DomainError(message: String, cause: Option[Throwable] = None)
    extends AppError(message, cause)
    with NoStackTrace
abstract class FatalError(message: String, cause: Option[Throwable] = None)
    extends AppError(message, cause)

object FatalErrors:
  class DataInconsistencyError(violations: Result.Errors)
      extends FatalError(
        s"Inconsistent data at: ${violations.asErrorPathMessages}",
      )
