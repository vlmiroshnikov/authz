package io.github.vmiroshnikov.authz.jwt.test

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

import io.github.vmiroshnikov.authz.jwt.{given, _}
import io.github.vmiroshnikov.authz.jwt.impl.RS256
import io.github.vmiroshnikov.authz.jwt.circe.{given, _}
import io.github.vmiroshnikov.authz.utils._
import io.github.vmiroshnikov.authz._

object SimpleApp extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    
    val keyPair = JCAHelper.generateRSAKeyPair

    given s as Signer[IO] = RS256.signer[IO](keyPair.getPrivate())
    given v as Verifier[IO] = RS256.verifier[IO](keyPair.getPublic())
    for
      jwt <- buildAndSign[IO, StdHeader, StdClaims](StdHeader(algorithm = v.algo.name), StdClaims())
      _   <- IO.delay(println(jwt.show))
      res <- parse[IO, StdHeader, StdClaims](jwt.show)
      v   <- verify[IO, StdHeader, StdClaims](res.header, res.claims, res.signature, res.signedPart)
      _   <- IO.delay(println(res))  
      _   <- IO.delay(println("Result: " + v.toString))  
    yield ExitCode.Success
  }
}