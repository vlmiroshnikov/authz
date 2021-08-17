package io.github.vlmiroshnikov.authz.jwt

import cats.syntax.all.*
import cats.effect.*

import io.github.vlmiroshnikov.authz.jwt.{given, *}
import io.github.vlmiroshnikov.authz.jwt.impl.RS256
import io.github.vlmiroshnikov.authz.jwt.circe.{given, *}
import io.github.vlmiroshnikov.authz.utils.*

object SimpleApp extends IOApp.Simple {

  def run: IO[Unit] = {
    
    val keyPair = generateRSAKeyPair

    given s: Signer[IO]   = RS256.signer[IO](keyPair.getPrivate())
    given v: Verifier[IO] = RS256.verifier[IO](keyPair.getPublic())

    for
      jwt <- buildAndSign[IO, StdHeader, StdClaims](StdHeader(algorithm = v.algo.name), StdClaims())
      _   <- IO.println(jwt.show)
      res <- parse[IO, StdHeader, StdClaims](jwt.show)
      vr  <- verify[IO, StdHeader, StdClaims](res.header, res.claims, res.signature, res.signedPart)
      _   <- IO.println(res.toString)
      _   <- IO.println(vr.toString)
    yield ()
  }
}