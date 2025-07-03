package org.sarden.domain.user

import zio.*

import org.sarden.InvalidRequestError
import org.sarden.bindings.*
import org.sarden.bindings.tx.*
import org.sarden.bindings.{Password, PasswordFormat, PasswordHasher}
import org.sarden.domain.user.internal.{LiveUserRepo, UserRepo}

case class AuthenticationFailedError()
    extends InvalidRequestError("Failed to authenticate the request")

trait UserService:
  def getUserByCredentials(
      username: UserName,
      password: Password[PasswordFormat.Plain],
  ): IO[AuthenticationFailedError, User]

object UserService:
  val live: URLayer[PasswordHasher & Tx.Runner, UserService] =
    ZLayer.fromZIO:
      for
        tx <- ZIO.service[Tx.Runner]
        hasher <- ZIO.service[PasswordHasher]
        repo = LiveUserRepo()
      yield LiveUserService(repo, tx, hasher)

class LiveUserService(repo: UserRepo, tx: Tx.Runner, hasher: PasswordHasher)
    extends UserService:
  override def getUserByCredentials(
      username: UserName,
      password: Password[PasswordFormat.Plain],
  ): IO[AuthenticationFailedError, User] =
    for
      user <- tx.transactionOrDie:
        repo
          .getUserByName(username)
          .someOrFail(AuthenticationFailedError())
      _ <- ZIO.unlessZIO(hasher.isHashValid(password, user.password)):
        ZIO.fail(AuthenticationFailedError())
    yield user
