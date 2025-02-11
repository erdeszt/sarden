package org.sarden.core.user

import neotype.*

import org.sarden.core.*
import org.sarden.core.ulid.*

case class User(
    id: UserId,
    name: UserName,
    password: Password[PasswordFormat.Hashed],
)

type UserId = UserId.Type
object UserId extends UlidNewtype

type UserName = UserName.Type
object UserName extends Newtype[String]
