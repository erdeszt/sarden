package org.sarden.core

import scala.util.control.NoStackTrace

import io.scalaland.chimney.partial.Result

// TODO: Nice error types with messages/causes
// TODO: Restructure it, this split is not not ideal
//        * Example: InvalidTimeUnitError can be domain error if it comes from
//                   the api and we try to conver to domain types
//                   but it can also be a system error when we read an incorrect value from the db
// Maybe use: ConstraintVoilationError{External,Internal} UnexpectedError(underlying: Throwable)
sealed abstract class AppError(message: String, cause: Option[Throwable])
    extends Exception:
  override def getCause: Throwable | Null = cause.orNull
  override def getMessage: String = message
abstract class DomainError(message: String, cause: Option[Throwable] = None)
    extends AppError(message, cause)
    with NoStackTrace
abstract class SystemError(message: String, cause: Option[Throwable] = None)
    extends AppError(message, cause)

object DomainErrors:
  class AuthenticationFailedError()
      extends DomainError("Failed to authenticate")

object SystemErrors:
  class DataInconsistencyError(violations: Result.Errors)
      extends SystemError(
        s"Inconsistent data at: ${violations.asErrorPathMessages}",
      )
  class InvalidTimeUnitError(rawValue: String)
      extends SystemError(s"Invalid TimeUnit: `${rawValue}`")
