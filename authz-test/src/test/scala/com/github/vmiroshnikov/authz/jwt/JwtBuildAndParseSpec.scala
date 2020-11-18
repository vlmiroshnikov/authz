package com.github.vmiroshnikov.authz.jwt

import org.scalatest.matchers.must.Matchers
import org.scalatest.flatspec.AnyFlatSpec

import com.github.vmiroshnikov.authz.jwt.{given, _}
import com.github.vmiroshnikov.authz.jwt.impl.RS256
import com.github.vmiroshnikov.authz.jwt.circe.{given, _}

trait TestSpec extends AnyFlatSpec with Matchers

class JwtBuildAndParseSpec extends TestSpec {

  type R[A] = Either[Throwable, A]

  it should "sign with RSA " in {
    ???
  }
}
