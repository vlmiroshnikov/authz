package com.github.vmiroshnikov.authz.jwt

import org.apache.commons.codec.binary.Base64
import java.nio.charset.StandardCharsets
import java.time.Instant
import scala.util.control.NoStackTrace
import scala.util.Try

import cats.data.NonEmptyList
import cats._
import cats.syntax._
import cats.implicits._

trait Alg(val name: String)

case class Jwt(header: String, claims: String, signature: String) 

object Jwt {
    given Show[Jwt]:
        def show(jwt: Jwt): String = 
            jwt.header + "." + jwt.claims + "." + jwt.signature 
}

enum JWTType(val name: String) {
    case JWT extends JWTType("JWT")
}

opaque type Audience = NonEmptyList[String]
object Audience {
    def fromList(lst: List[String]): Option[Audience] = NonEmptyList.fromList(lst)
    extension (a: Audience) def toList: List[String] = a.toList
}

trait Header{
    def `type`: JWTType //Type, which will almost always default to "JWT"
    def contentType: Option[String] // Optional header, preferably not used
    def critical: Option[NonEmptyList[String]] //Headers not to ignore, they must be understood by the JWT implementation
    def jku: Option[String] //Resource set for JWK
    def jwk: Option[String] //JWK
    def kid: Option[String] //JWK key hint
    def x5u: Option[String] //The "x5c" (X.509 certificate chain) Header Parameter
    def algorithm: String
}

case class StdHeader(
    `type`: JWTType = JWTType.JWT,
    algorithm: String,
    contentType: Option[String] = None, // Optional header, preferably not used
    critical: Option[NonEmptyList[String]] = None, //Headers not to ignore, they must be understood by the JWT implementation
    jku: Option[String] = None, //Resource set for JWK
    jwk: Option[String] = None, //JWK
    kid: Option[String] = None, //JWK key hint
    x5u: Option[String] = None, //The "x5c" (X.509 certificate chain) Header Parameter
) extends Header

trait Claims{
    def issuer: Option[String]
    def subject: Option[String]
    def audience: Option[Audience]       //case-sensitive
    def expiration: Option[Instant]
    def notBefore: Option[Instant]          // IEEE Std 1003.1, 2013 Edition time in seconds
    def issuedAt: Option[Instant]          // IEEE Std 1003.1, 2013 Edition time in seconds
    def jwtId: Option[String]               //Case sensitive, and in our implementation, secure enough using UUIDv4
}

case class StdClaims(
    val issuer: Option[String] = None,
    val subject: Option[String] = None,
    val audience: Option[Audience] = None,      //case-sensitive
    val expiration: Option[Instant] = None,
    val notBefore: Option[Instant] = None,         // IEEE Std 1003.1, 2013 Edition time in seconds
    val issuedAt: Option[Instant] = None,          // IEEE Std 1003.1, 2013 Edition time in seconds
    val jwtId: Option[String]= None               //Case sensitive, and in our implementation, secure enough using UUIDv4
) extends Claims

opaque type Binary = Array[Byte]

object Binary {
    def apply(data: Array[Byte]): Binary = data
    extension (x: Binary) def toArray: Array[Byte] = x
}

trait Signer[F[_]]:
    type Repr <: Alg
    def algo: Repr
    def sign(data: Binary): F[Binary]


trait Verifier[F[_]]:
    type Repr <: Alg    
    def algo: Repr
    def verify(signature: Binary, data: Binary): F[Boolean]

case object DecodingFailure extends Throwable with NoStackTrace
case object EncodingFailure extends Throwable with NoStackTrace

case class UnsupportedAlgorithm(alg: String) extends Exception(s"Unsupported algorithm: ${alg}") with NoStackTrace
case object InvalidSignature extends Exception("Invalid signature") with NoStackTrace
case class InvalidJWTFormat(msg: String) extends Exception("Invalid JWT format: "  + msg) with NoStackTrace


// todo rename into intermediate encoder
trait AuxEncoder[A]:
    def encode(a: A): Either[EncodingFailure.type, Binary]

// todo rename into intermediate encoder
trait AuxDecoder[A]:
    def decode(a: Binary): Either[DecodingFailure.type, A]

def buildAndSign[F[_], H <: Header, C <: Claims](header: H, claims: C)
        (using E: MonadError[F, Throwable],
            signer: Signer[F], 
            headerEnc: AuxEncoder[H], 
            claimsEnc: AuxEncoder[C]): F[Jwt] = {

    def asciiBytes(s: String): Binary = Binary(s.getBytes(StandardCharsets.US_ASCII))
    def encodeToString(bin: Binary) = E.fromTry(Try(Base64.encodeBase64URLSafeString(bin.toArray)))

    for 
        headerBin       <- E.fromEither(headerEnc.encode(header)) >>= encodeToString
        claimsBin       <- E.fromEither(claimsEnc.encode(claims)) >>= encodeToString
        toSign          <- E.pure(headerBin + "." + claimsBin)
        signature       <- signer.sign(asciiBytes(toSign))
        signatureBin    <- encodeToString(signature)
    yield Jwt(headerBin, claimsBin, signatureBin)
}



case class ParseResult[H <: Header, C <: Claims](header: H, claims: C, signature: Binary, signedPart: String)

def parse[F[_], H <: Header, C <: Claims](jwt: String)(
    using E: MonadError[F, Throwable], 
            headerDec: AuxDecoder[H], 
            claimsDec: AuxDecoder[C]): F[ParseResult[H, C]] = {

    type Unmarshalable[A] = AuxDecoder[A] ?=> F[A]

    def decodeFromString(s: String) = E.fromTry(Try(Base64.decodeBase64(s)))
    def decode[A](b: Binary): Unmarshalable[A] = E.fromEither(summon[AuxDecoder[A]].decode(b))

    jwt.split("\\.").toList match
        case header :: claims :: signature :: Nil => 
            for 
                h <- decodeFromString(header) >>= decode[H]
                c <- decodeFromString(claims) >>= decode[C]
                s <- decodeFromString(signature)
            yield ParseResult(h, c, s, header + "." + claims)
        case lst => E.raiseError(InvalidJWTFormat(s"Parts ${lst.length}"))
}

extension (c: Claims):
    def isNotExpired(now: Instant): Boolean = c.expiration.forall(e => now.isBefore(e))
    def isAfterNBF(now: Instant): Boolean   = c.notBefore.forall(e => now.isAfter(e))
    def isValidIssued(now: Instant): Boolean = c.issuedAt.forall(e => !now.isBefore(e))
    

def verify[F[_], H <: Header, C <: Claims](header: H, claims: C, signature: Binary, signedPart: String)
                                        (using E: MonadError[F, Throwable], verifier: Verifier[F]): F[Boolean] = {
    
    def asciiBytes(s: String): Binary = Binary(s.getBytes(StandardCharsets.US_ASCII))                   

    val checkClaims = E.pure(Instant.now())
                        .map(now => claims.isNotExpired(now) && claims.isAfterNBF(now) && claims.isValidIssued(now))

    for 
      _     <- E.raiseError(UnsupportedAlgorithm(header.algorithm)).whenA(header.algorithm != verifier.algo.name)
      valid <- List(verifier.verify(signature, asciiBytes(signedPart)), checkClaims).forallM(identity)
    yield valid
}
