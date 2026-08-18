package gls.domain.user

import scala.language.experimental.saferExceptions

import neotype.*

import gls.*

trait UserService {

  def createUser(
      email: Email,
      password: PlainPassword,
  ): User throws EmailFormatError | WeakPasswordError

  def login(email: Email, password: PlainPassword): Option[User]

}

class LiveUserService(
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
    val now = clock.now()
    val hashedPassword = passwordHasher.hashPassword(password)
    val user = User(id, email, hashedPassword)

    repo.create(user)

    user
  }

  def login(email: Email, password: PlainPassword): Option[User] = {
    repo
      .getByEmail(email)
      .filter(user =>
        passwordHasher.isPasswordHashMatching(user.password, password),
      )
  }

}
