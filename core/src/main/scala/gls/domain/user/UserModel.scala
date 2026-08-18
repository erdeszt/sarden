package gls.domain.user

import com.github.f4b6a3.ulid.Ulid
import neotype.*

import gls.*

case class User(
    id: UserId,
    email: Email,
    password: HashedPassword,
)

type UserId = UserId.Type
object UserId extends Newtype[Ulid]

type Email = Email.Type
object Email extends Newtype[String] derives CanEqual
