package org.sarden.core

sealed abstract class AppError(message: String, cause: Option[Throwable])
    extends Exception:
  override def getCause: Throwable | Null = cause.orNull
  override def getMessage: String = message

abstract class InvalidRequestError(
    message: String,
    cause: Option[Throwable] = None,
) extends AppError(message, cause)

sealed abstract class InternalError(
    message: String,
    cause: Option[Throwable],
) extends AppError(message, cause)

abstract class InvariantViolationError(
    message: String,
    cause: Option[Throwable] = None,
) extends InternalError(message, cause)

abstract class SystemError(message: String, cause: Throwable)
    extends InternalError(message, Some(cause))
