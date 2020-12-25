package io.github.vmiroshnikov.authz.jwt

import io.github.vmiroshnikov.authz.jwt.{given, _}
import io.github.vmiroshnikov.authz.jwt.impl.RS256
import io.github.vmiroshnikov.authz.jwt.circe.{given, _}

class JwtBuildAndParseSuite extends munit.FunSuite {

  type R[A] = Either[Throwable, A]

  test("sign with RSA") {
    fail("")
  }
}
