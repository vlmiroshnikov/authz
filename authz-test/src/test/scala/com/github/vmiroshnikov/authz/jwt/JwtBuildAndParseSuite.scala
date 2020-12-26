package io.github.vmiroshnikov.authz.jwt

import cats.implicits._
import io.github.vmiroshnikov.authz.jwt.{given, _}
import io.github.vmiroshnikov.authz.jwt.impl.RS256
import io.github.vmiroshnikov.authz.utils._
import io.github.vmiroshnikov.authz._
import io.github.vmiroshnikov.authz.jwt.circe.{given, _}

class JwtBuildAndParseSuite extends munit.FunSuite {

  type R[A] = Either[Throwable, A]

  test("verify RS256") {
    val keyPair = JCAHelper.generateRSAKeyPair

    given s: Signer[R] = RS256.signer[R](keyPair.getPrivate())
    given v: Verifier[R] = RS256.verifier[R](keyPair.getPublic())
    val result = for
      jwt <- buildAndSign[R, StdHeader, StdClaims](StdHeader(algorithm = v.algo.name), StdClaims())
      res <- parse[R, StdHeader, StdClaims](jwt.show)
      v   <- verify[R, StdHeader, StdClaims](res.header, res.claims, res.signature, res.signedPart)
    yield v

    assertEquals(result, Right(true))
  }

  test("verify with wrong public key RS256") {
    val privateKey = JCAHelper.generateRSAKeyPair.getPrivate()
    val publicKey  = JCAHelper.generateRSAKeyPair.getPublic()

    given s: Signer[R] = RS256.signer[R](privateKey)
    given v: Verifier[R] = RS256.verifier[R](publicKey)
    val result = for
      jwt <- buildAndSign[R, StdHeader, StdClaims](StdHeader(algorithm = v.algo.name), StdClaims())
      res <- parse[R, StdHeader, StdClaims](jwt.show)
      v   <- verify[R, StdHeader, StdClaims](res.header, res.claims, res.signature, res.signedPart)
    yield v

    assertEquals(result, Right(false))
  }
}
