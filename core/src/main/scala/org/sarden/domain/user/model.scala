package org.sarden.domain.user

import neotype.*

import org.sarden.*
import org.sarden.bindings.{Password, PasswordFormat}
import org.sarden.bindings.ulid.*

case class User(
    id: UserId,
    name: UserName,
    password: Password[PasswordFormat.Hashed],
)

type UserId = UserId.Type
object UserId extends UlidNewtype

type UserName = UserName.Type
object UserName extends Newtype[String]
