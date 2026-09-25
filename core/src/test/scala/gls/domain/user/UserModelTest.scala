package gls.domain.user

import org.scalatest.funspec.*

class UserModelTest extends AnyFunSpec {

  describe("Email") {
    it("should validate email") {
      assert(Email("no_at").isEmpty)
    }
  }
}
