package io.github.vlmiroshnikov.authz.jwt.circe

import java.nio.charset.StandardCharsets
import java.time.Instant
import scala.util.Try
import cats.syntax.all.*
import cats.data.NonEmptyList
import io.circe.{ DecodingFailure as CDecodingFailure, * }
import io.circe.syntax.*

import io.github.vlmiroshnikov.authz.jwt.*

object ClaimsKeys {
  val Issuer: String     = "iss"
  val Subject: String    = "sub"
  val Audience: String   = "aud"
  val Expiration: String = "exp"
  val NotBefore: String  = "nbf"
  val IssuedAt: String   = "iat"
  val JwtId: String      = "jti"
}

given Encoder[StdHeader] with

  def apply(a: StdHeader): Json =
    Json.obj(
      ("typ", a.`type`.name.asJson),
      ("alg", a.algorithm.asJson),
      ("cty", a.contentType.asJson),
      ("crit", a.critical.asJson),
      ("jku", a.jku.asJson),
      ("jwk", a.jwk.asJson),
      ("kid", a.kid.asJson),
      ("x5u", a.x5u.asJson)
    )

given Decoder[JWTType] = Decoder.decodeString.emap { s =>
  if s == "JWT" then JWTType.JWT.asRight else s"Unsupported jwt type $s".asLeft
}

given Decoder[StdHeader] = Decoder.instance { c =>
  for
    typ  <- c.downField("typ").as[JWTType]
    alg  <- c.downField("alg").as[String]
    cty  <- c.downField("cty").as[Option[String]]
    crit <- c.downField("crit").as[Option[List[String]]]
    jku  <- c.downField("jku").as[Option[String]]
    jwk  <- c.downField("jwk").as[Option[String]]
    kid  <- c.downField("kid").as[Option[String]]
    x5u  <- c.downField("x5u").as[Option[String]]
  yield StdHeader(typ, alg, cty, crit.flatMap(v => NonEmptyList.fromList(v)), jku, jwk, kid, x5u)
}

given Encoder[StdClaims] with {

  import ClaimsKeys._

  def apply(c: StdClaims): Json =
    Json.obj(
      (Issuer, c.issuer.asJson),
      (Subject, c.subject.asJson),
      (Audience, c.audience.map(_.toList).asJson),
      (Expiration, c.expiration.map(_.getEpochSecond).asJson),
      (NotBefore, c.notBefore.map(_.getEpochSecond).asJson),
      (IssuedAt, c.issuedAt.map(_.getEpochSecond).asJson),
      (JwtId, c.jwtId.asJson)
    )
}

given Decoder[Audience] = Decoder.instance { cur =>
  cur
    .as[String]
    .map(v => List(v))
    .orElse(cur.as[List[String]])
    .flatMap(v => Audience.fromList(v).toRight(CDecodingFailure("Invalid audience", Nil)))
}

given Decoder[StdClaims] = Decoder.instance { c =>
  import ClaimsKeys._

  def unsafeInstant(i: Option[Long]): Decoder.Result[Option[Instant]] =
    i.fold(None.asRight) { v =>
      Either
        .fromTry(Try(Instant.ofEpochSecond(v)))
        .leftMap(_ => CDecodingFailure("invalid date", Nil))
        .map(_.some)
    }

  for
    iss <- c.downField(Issuer).as[Option[String]]
    sub <- c.downField(Subject).as[Option[String]]
    aud <- c.downField(Audience).as[Option[Audience]]
    exp <- c.downField(Expiration).as[Option[Long]].flatMap(unsafeInstant)
    nbf <- c.downField(NotBefore).as[Option[Long]].flatMap(unsafeInstant)
    iat <- c.downField(IssuedAt).as[Option[Long]].flatMap(unsafeInstant)
    jti <- c.downField(JwtId).as[Option[String]]
  yield StdClaims(iss, sub, aud, exp, nbf, iat, jti)
}

given [A: Encoder]: AuxEncoder[A] with {
  private val printer = Printer(dropNullValues = true, indent = "")

  def encode(a: A): Either[EncodingFailure, Binary] =
    val data = a.asJson.printWith(printer)
    Right(Binary(data.getBytes(StandardCharsets.UTF_8)))
}

given [A: Decoder]: AuxDecoder[A] with {
  import io.circe.parser.parse

  def decode(bin: Binary): Either[DecodingFailure, A] =
    parse(new String(bin.toArray))
      .flatMap(_.as[A])
      .leftMap(e => DecodingFailure(e.getMessage))
}
