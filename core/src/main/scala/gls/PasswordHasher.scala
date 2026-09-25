package gls

import at.favre.lib.crypto.bcrypt.BCrypt

import gls.domain.*

opaque type HashedPassword = String
object HashedPassword extends Newtype[HashedPassword, String]

opaque type PlainPassword = String
object PlainPassword extends Newtype[PlainPassword, String]

trait PasswordHasher {

  def hashPassword(plain: PlainPassword): HashedPassword

  def isPasswordHashMatching(hashed: HashedPassword, plain: PlainPassword): Boolean

}

class BCryptPasswordHasher extends PasswordHasher {

  private val hasher = BCrypt.withDefaults()
  private val verifier = BCrypt.verifyer()

  def hashPassword(plain: PlainPassword): HashedPassword = {
    HashedPassword(hasher.hashToString(12, plain.unwrap.toCharArray()))
  }

  def isPasswordHashMatching(hashed: HashedPassword, plain: PlainPassword): Boolean = {
    verifier.verify(plain.unwrap.toCharArray(), hashed.unwrap.toCharArray()).verified
  }

}
