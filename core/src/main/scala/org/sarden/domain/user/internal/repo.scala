package org.sarden.domain.user.internal

import zio.*

import org.sarden.bindings.tx.*
import org.sarden.domain.user.*
import org.sarden.domain.user.{User, UserName}
import org.sarden.bindings.mapping.given

private[user] trait UserRepo:
  def getUserByName(username: UserName): URIO[Tx, Option[User]]

class LiveUserRepo extends UserRepo:
  override def getUserByName(username: UserName): URIO[Tx, Option[User]] =
    Tx {
      sql"SELECT id, name, password FROM user WHERE name = ${username}"
        .queryThrough[UserDTO, User]
        .option
    }
