package org.sarden.core.user.internal

import zio.*

import org.sarden.core.mapping.given
import org.sarden.core.tx.*
import org.sarden.core.user.*

private[user] trait UserRepo:
  def getUserByName(username: UserName): URIO[Tx, Option[User]]

class LiveUserRepo extends UserRepo:
  override def getUserByName(username: UserName): URIO[Tx, Option[User]] =
    Tx {
      sql"SELECT id, name, password FROM user WHERE name = ${username}"
        .queryThrough[UserDTO, User]
        .option
    }
