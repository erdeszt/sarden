package gls.domain.user

import scala.language.experimental.saferExceptions

import neotype.*

import gls.*

trait UserService {

  def createUser(
      email: Email,
      password: PlainPassword,
  ): User throws EmailFormatError | WeakPasswordError

  def getByCredentials(email: Email, password: PlainPassword): Option[User]

  def getById(id: UserId)(using userCtx: UserCtx[UserRole.Admin]): Option[User]

  def getSelf[Role <: UserRole.Basic](using userCtx: UserCtx[Role]): User

}

object UserService {
  def live(
      idGenerator: IdGenerator[UserId],
      clock: Clock,
      passwordHasher: PasswordHasher,
  ): UserService = {
    LiveUserService(
      InMemoryUserRepo(),
      idGenerator,
      clock,
      passwordHasher,
    )
  }
}

private[user] class LiveUserService(
    repo: UserRepo,
    idGenerator: IdGenerator[UserId],
    clock: Clock,
    passwordHasher: PasswordHasher,
) extends UserService {

  override def createUser(
      email: Email,
      password: PlainPassword,
  ): User throws EmailFormatError | WeakPasswordError = {
    check(email.unwrap.contains('@'), EmailFormatError())
    check(password.unwrap.length >= 8, WeakPasswordError())

    val id = idGenerator.generate()
    val _ = clock.now()
    val hashedPassword = passwordHasher.hashPassword(password)
    val user = User(id, email, hashedPassword, UserRole.Basic())

    repo.create(user)

    user
  }

  override def getByCredentials(
      email: Email,
      password: PlainPassword,
  ): Option[User] = {
    repo
      .getByEmail(email)
      .filter(user =>
        passwordHasher.isPasswordHashMatching(user.password, password),
      )
  }

  override def getById(
      id: UserId,
  )(using UserCtx[UserRole.Admin]): Option[User] = {
    repo.getById(id)
  }

  override def getSelf[Role <: UserRole.Basic](using
      ctx: UserCtx[Role],
  ): User = {
    repo.getById(ctx.userId) match {
      case None =>
        throw SelfNotFoundError(ctx.userId)
      case Some(user) =>
        user
    }

  }

}
