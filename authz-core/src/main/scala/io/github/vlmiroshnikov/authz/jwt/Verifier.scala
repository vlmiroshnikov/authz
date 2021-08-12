package io.github.vlmiroshnikov.authz.jwt

trait Verifier[F[_]]:
  type Repr <: Alg
  def algo: Repr
  def verify(signature: Binary, data: Binary): F[Boolean]
