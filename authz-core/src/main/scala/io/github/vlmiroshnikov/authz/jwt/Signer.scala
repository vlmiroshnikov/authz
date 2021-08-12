package io.github.vlmiroshnikov.authz.jwt

trait Signer[F[_]]:
  type Repr <: Alg
  def algo: Repr
  def sign(data: Binary): F[Binary]
