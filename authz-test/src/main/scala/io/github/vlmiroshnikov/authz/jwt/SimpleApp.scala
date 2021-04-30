package io.github.vlmiroshnikov.authz.jwt

import cats.effect._
import cats.implicits._

import io.github.vlmiroshnikov.authz.jwt.{given, _}
import io.github.vlmiroshnikov.authz.jwt.impl.RS256
import io.github.vlmiroshnikov.authz.jwt.circe.{given, _}
import io.github.vlmiroshnikov.authz.utils._

object SimpleApp extends IOApp.Simple {

  def run: IO[Unit] = {
    
    val keyPair = JCAHelper.generateRSAKeyPair

    given s: Signer[IO]   = RS256.signer[IO](keyPair.getPrivate())
    given v: Verifier[IO] = RS256.verifier[IO](keyPair.getPublic())

    for
      jwt <- buildAndSign[IO, StdHeader, StdClaims](StdHeader(algorithm = v.algo.name), StdClaims())
      _   <- IO.println(jwt.show)
      res <- parse[IO, StdHeader, StdClaims](jwt.show)
      v   <- verify[IO, StdHeader, StdClaims](res.header, res.claims, res.signature, res.signedPart)
      _   <- IO.println(res.toString)
      _   <- IO.println("Result: " + v.show)
    yield ()
  }
}