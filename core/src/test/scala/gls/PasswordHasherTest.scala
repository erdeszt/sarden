package gls

import neotype.*
import org.scalatest.funspec.AnyFunSpec

class BCryptPasswordHasherTest
    extends PasswordHasherTest(BCryptPasswordHasher())
class IdentityPasswordHasherTest
    extends PasswordHasherTest(IdentityPasswordHasher())

abstract class PasswordHasherTest(hasher: PasswordHasher) extends AnyFunSpec {

  describe(s"PasswordHasher(${hasher.getClass.getName})") {
    it("should verify the hash that it generates") {
      val password = PlainPassword("test")

      hasher.isPasswordHashMatching(hasher.hashPassword(password), password)
    }
  }

}

class IdentityPasswordHasher extends PasswordHasher {

  override def hashPassword(plain: PlainPassword): HashedPassword = {
    HashedPassword(plain.unwrap)
  }

  override def isPasswordHashMatching(
      hashed: HashedPassword,
      plain: PlainPassword,
  ): Boolean = {
    hashed.unwrap == plain.unwrap
  }

}
