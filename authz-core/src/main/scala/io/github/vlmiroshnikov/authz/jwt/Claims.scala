package io.github.vlmiroshnikov.authz.jwt

import java.time.Instant
import cats.data.*

opaque type Audience = NonEmptyList[String]

object Audience:
  def fromList(lst: List[String]): Option[Audience] = NonEmptyList.fromList(lst)
  extension (a: Audience) def toList: List[String]  = a.toList

trait Claims:
  def issuer: Option[String]
  def subject: Option[String]
  def audience: Option[Audience]
  def expiration: Option[Instant]
  def notBefore: Option[Instant]
  def issuedAt: Option[Instant]
  def jwtId: Option[String]

case class StdClaims(
    val issuer: Option[String] = None,
    val subject: Option[String] = None,
    val audience: Option[Audience] = None,
    val expiration: Option[Instant] = None,
    val notBefore: Option[Instant] = None,
    val issuedAt: Option[Instant] = None,
    val jwtId: Option[String] = None)
    extends Claims
