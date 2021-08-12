package io.github.vlmiroshnikov.authz.jwt

import cats.data.NonEmptyList

enum JWTType(val name: String):
  case JWT extends JWTType("JWT")

trait Header:
  def `type`: JWTType
  def contentType: Option[String]
  def critical: Option[NonEmptyList[String]]
  def jku: Option[String]
  def jwk: Option[String]
  def kid: Option[String]
  def x5u: Option[String]
  def algorithm: String

case class StdHeader(
    `type`: JWTType = JWTType.JWT,
    algorithm: String,
    contentType: Option[String] = None,
    critical: Option[NonEmptyList[String]] = None,
    jku: Option[String] = None,
    jwk: Option[String] = None,
    kid: Option[String] = None,
    x5u: Option[String] = None)
    extends Header
