package gls.domain.user

import gls.*

trait UserService {

  def createUser(email: Email, password: PlainPassword): User

  def login(email: Email, password: PlainPassword): Option[User]

}

class LiveUserService(
    repo: UserRepo,
    idGenerator: IdGenerator[UserId],
    clock: Clock,
    passwordHasher: PasswordHasher,
) extends UserService {

  override def createUser(email: Email, password: PlainPassword): User = {
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
