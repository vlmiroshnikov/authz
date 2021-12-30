package io.github.vlmiroshnikov.authz.jwt

import cats.*
import cats.data.*
import cats.syntax.all.*
import org.apache.commons.codec.binary.Base64

import java.nio.charset.StandardCharsets
import java.time.Instant
import scala.util.Try
import scala.util.control.NoStackTrace

case class Jwt[H <: Header, C <: Claims](header: H, claims: C, signature: Binary)

case class InvalidJWTFormat(msg: String)
    extends Exception("Invalid JWT format: " + msg)
    with NoStackTrace

def buildAndSign[F[_], H <: Header, C <: Claims](
    header: H,
    claims: C
  )(using
    E: MonadError[F, Throwable],
    signer: Signer[F],
    headerEnc: AuxEncoder[H],
    claimsEnc: AuxEncoder[C]): F[RawJwt] = {

  def asciiBytes(s: String): Binary = Binary(s.getBytes(StandardCharsets.US_ASCII))
  def encodeToString(bin: Binary)   = E.fromTry(Try(Base64.encodeBase64URLSafeString(bin.toArray)))

  for
    headerBin    <- E.fromEither(headerEnc.encode(header)) >>= encodeToString
    claimsBin    <- E.fromEither(claimsEnc.encode(claims)) >>= encodeToString
    toSign       <- E.pure(headerBin + "." + claimsBin)
    signature    <- signer.sign(asciiBytes(toSign))
    signatureBin <- encodeToString(signature)
  yield RawJwt(headerBin, claimsBin, signatureBin)
}

case class ParseResult[H <: Header, C <: Claims](
    header: H,
    claims: C,
    signature: Binary,
    signedPart: String)

def parse[F[_], H <: Header, C <: Claims](
    jwt: String
  )(using
    E: MonadError[F, Throwable],
    headerDec: AuxDecoder[H],
    claimsDec: AuxDecoder[C]): F[ParseResult[H, C]] = {

  type Unmarshalable[A] = AuxDecoder[A] ?=> F[A]

  def decodeFromString(s: String)            = E.fromTry(Try(Binary(Base64.decodeBase64(s))))
  def decode[A](b: Binary): Unmarshalable[A] = decoder ?=> E.fromEither(decoder.decode(b))

  jwt.split("\\.").toList match
    case header :: claims :: signature :: Nil =>
      for
        h <- decodeFromString(header) >>= decode[H]
        c <- decodeFromString(claims) >>= decode[C]
        s <- decodeFromString(signature)
      yield ParseResult(h, c, s, header + "." + claims)
    case lst => E.raiseError(InvalidJWTFormat("Expected header.body.signature"))
}

extension (c: Claims)
  def isNotExpired(now: Instant): Boolean  = c.expiration.forall(e => now.isBefore(e))
  def isAfterNBF(now: Instant): Boolean    = c.notBefore.forall(e => now.isAfter(e))
  def isValidIssued(now: Instant): Boolean = c.issuedAt.forall(e => !now.isBefore(e))

enum ValidationError:
  case Expired, BeforeIssueAt, EarlierNotBefore, InvalidSinature, UnsupportAlg

type ValidationResult = Either[ValidationError, Unit]

def verify[F[_]: Monad, H <: Header, C <: Claims](
    header: H,
    claims: C,
    signature: Binary,
    signedPart: String
  )(using
    verifier: Verifier[F]): F[ValidationResult] = {

  def asciiBytes(s: String): Binary = Binary(s.getBytes(StandardCharsets.US_ASCII))

  (for {
    now <- EitherT.liftF(Instant.now().pure[F])
    _ <- EitherT.fromEither(
           List(
             Either.cond(claims.isNotExpired(now), (), ValidationError.Expired),
             Either.cond(claims.isAfterNBF(now), (), ValidationError.EarlierNotBefore),
             Either.cond(claims.isValidIssued(now), (), ValidationError.BeforeIssueAt),
             Either.cond(header.algorithm == verifier.algo.name, (), ValidationError.UnsupportAlg)
           ).traverse(identity))

    _ <- EitherT(
           verifier
             .verify(signature, asciiBytes(signedPart))
             .map(pass => Either.cond(pass, (), ValidationError.InvalidSinature)))
  } yield ()).value
}
