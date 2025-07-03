package org.sarden.core

import java.nio.charset.StandardCharsets

import at.favre.lib.crypto.bcrypt.BCrypt
import zio.*

sealed abstract class PasswordFormat
object PasswordFormat:
  case class Plain() extends PasswordFormat
  case class Hashed() extends PasswordFormat

case class Password[Format <: PasswordFormat](value: String) extends AnyVal

trait PasswordHasher:
  def hashPassword(
      password: Password[PasswordFormat.Plain],
  ): UIO[Password[PasswordFormat.Hashed]]
  def isHashValid(
      attempt: Password[PasswordFormat.Plain],
      source: Password[PasswordFormat.Hashed],
  ): UIO[Boolean]

object PasswordHasher:
  // TODO: Move hash cost to config
  // TODO: Add charset to config
  val live: ULayer[PasswordHasher] = ZLayer.succeed(LivePasswordHasher(12))

class LivePasswordHasher(cost: Int) extends PasswordHasher:
  override def hashPassword(
      password: Password[PasswordFormat.Plain],
  ): UIO[Password[PasswordFormat.Hashed]] =
    ZIO.attempt {
      Password(
        String(
          BCrypt
            .withDefaults()
            .hash(cost, password.value.toCharArray),
          StandardCharsets.UTF_8,
        ),
      )
    }.orDie

  override def isHashValid(
      attempt: Password[PasswordFormat.Plain],
      source: Password[PasswordFormat.Hashed],
  ): UIO[Boolean] =
    ZIO.attempt {
      BCrypt.verifyer().verify(attempt.value.toCharArray, source.value).verified
    }.orDie
