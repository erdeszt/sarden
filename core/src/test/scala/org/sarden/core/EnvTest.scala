package org.sarden.core

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class EnvTest extends AnyFlatSpec with Matchers {

  "An empty environment" should "not allow lookup" in {
    "EnvBuilder.empty.build.get[String]" shouldNot compile
  }

  it should "not allow lookup of types not in the environment" in {
    "EnvBuilder.empty.add[String](\"foo\").get[Int]" shouldNot compile
  }

  it should "return the added value for a type" in {
    val env = EnvBuilder.empty.add[String]("foo").build

    val result = env.get[String]

    assert(result == "foo")
  }

  it should "allow overriding dependencies" in {
    val originalEnv = EnvBuilder.empty.add[String]("foo")
    val updatedEnv = originalEnv.add[String]("bar")

    val originalValue = originalEnv.build.get[String]
    val updatedValue = updatedEnv.build.get[String]

    assert(originalValue == "foo")
    assert(updatedValue == "bar")
  }

}
