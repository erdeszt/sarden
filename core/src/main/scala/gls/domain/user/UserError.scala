package gls.domain.user

case class EmailFormatError() extends Exception("invalid email format")
case class WeakPasswordError() extends Exception("weak password")
