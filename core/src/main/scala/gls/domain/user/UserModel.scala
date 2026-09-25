package gls.domain.user

import com.github.f4b6a3.ulid.Ulid

import gls.*
import gls.domain.*

case class User(
    id: UserId,
    email: Email,
    password: HashedPassword,
    role: UserRole,
)

opaque type UserId = Ulid
object UserId extends Newtype[UserId, Ulid]

opaque type Email = String
object Email extends NewtypeUnwrap[Email, String] {
  def apply(raw: String): Option[Email] = {
    if (raw.contains("@")) {
      Some(raw)
    } else {
      None
    }
  }
}

sealed trait UserRole
object UserRole {
  sealed trait Basic extends UserRole
  object Basic {
    def apply(): Basic = {
      new Basic {}
    }
  }
  sealed trait Admin extends Basic
  object Admin {
    def apply(): Admin = {
      new Admin {}
    }
  }
}

case class UserCtx[Role <: UserRole](userId: UserId)
