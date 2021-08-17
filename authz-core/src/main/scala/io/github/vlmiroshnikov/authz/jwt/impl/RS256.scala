package io.github.vlmiroshnikov.authz.jwt.impl

import java.security.{ MessageDigest, PrivateKey, PublicKey, Signature }
import javax.crypto.{ KeyGenerator, Mac, SecretKey }

import cats.*
import cats.syntax.all.*
import io.github.vlmiroshnikov.authz.jwt.*

object RS256 extends Alg(name = "RS256"):

  def signer[F[_]](key: PrivateKey)(using E: MonadError[F, Throwable]): Signer[F] =
    new Signer[F] {
      type Repr = RS256.type
      def algo: Repr = RS256

      def sign(data: Binary): F[Binary] =
        for
          rsa <- E.catchNonFatal(Signature.getInstance("SHA256withRSA"))
          _   <- E.catchNonFatal(rsa.initSign(key))
          _   <- E.catchNonFatal(rsa.update(data.toArray))
          sig <- E.catchNonFatal(rsa.sign())
        yield Binary(sig)
    }

  def verifier[F[_]](key: PublicKey)(using E: MonadError[F, Throwable]): Verifier[F] =
    new Verifier[F] {
      type Repr = RS256.type
      def algo: Repr = RS256

      def verify(signature: Binary, data: Binary): F[Boolean] = {
        for
          rsa <- E.catchNonFatal(Signature.getInstance("SHA256withRSA"))
          _   <- E.catchNonFatal(rsa.initVerify(key))
          _   <- E.catchNonFatal(rsa.update(data.toArray))
          v   <- E.catchNonFatal(rsa.verify(signature.toArray))
        yield v
      }
    }
end RS256
