package gls.domain.user

import com.github.f4b6a3.ulid.Ulid
import neotype.*

import gls.*

case class User(
    id: UserId,
    email: Email,
    password: HashedPassword,
    role: UserRole,
)

type UserId = UserId.Type
object UserId extends Newtype[Ulid]

type Email = Email.Type
object Email extends Newtype[String] derives CanEqual

sealed trait UserRole
object UserRole {
  sealed trait Basic extends UserRole
  object Basic {
    def apply(): Basic = {
      new Basic{}
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
