package gls

import at.favre.lib.crypto.bcrypt.BCrypt
import neotype.*
import neotype.unwrap

type HashedPassword = HashedPassword.Type
object HashedPassword extends Newtype[String]

type PlainPassword = PlainPassword.Type
object PlainPassword extends Newtype[String]

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
