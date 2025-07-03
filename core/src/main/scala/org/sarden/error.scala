package org.sarden

sealed abstract class AppError(message: String, cause: Option[Throwable])
    extends Exception:
  override def getCause: Throwable | Null = cause.orNull
  override def getMessage: String = message

abstract class InvalidRequestError(
    message: String,
    cause: Option[Throwable] = None,
) extends AppError(message, cause)

abstract class InternalError(
    message: String,
    cause: Option[Throwable] = None,
) extends AppError(message, cause)
