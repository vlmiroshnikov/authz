package io.github.vlmiroshnikov.authz.jwt

import cats.*

case class RawJwt(header: String, claims: String, signature: String)

object RawJwt:
  given Show[RawJwt] = Show.show(jwt => jwt.header + "." + jwt.claims + "." + jwt.signature)
