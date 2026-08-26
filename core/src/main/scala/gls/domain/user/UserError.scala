package gls.domain.user

import gls.domain.CodingError

sealed trait UserError

case class EmailFormatError()
    extends Exception("invalid email format")
    with UserError
case class WeakPasswordError() extends Exception("weak password") with UserError

case class SelfNotFoundError(userId: UserId)
    extends RuntimeException(s"self not found(userId=${userId})")
    with UserError
    with CodingError
