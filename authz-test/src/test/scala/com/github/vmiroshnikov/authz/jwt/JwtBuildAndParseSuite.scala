package com.github.vmiroshnikov.authz.jwt

import com.github.vmiroshnikov.authz.jwt.{given, _}
import com.github.vmiroshnikov.authz.jwt.impl.RS256
import com.github.vmiroshnikov.authz.jwt.circe.{given, _}

class JwtBuildAndParseSuite extends munit.FunSuite {

  type R[A] = Either[Throwable, A]

  test("sign with RSA".fail) {
    fail("")
  }
}
