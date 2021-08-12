package io.github.vlmiroshnikov.authz.jwt.impl

import java.security.{ MessageDigest, PrivateKey, PublicKey, Signature }
import javax.crypto.{ KeyGenerator, Mac, SecretKey }

import cats.MonadError
import cats.implicits._
import io.github.vlmiroshnikov.authz.jwt._

object HS256 extends Alg(name = "HS256"):

  def signer[F[_]](key: SecretKey)(using E: MonadError[F, Throwable]): Signer[F] =
    new Signer[F] {
      type Repr = HS256.type
      def algo: Repr = HS256

      def sign(data: Binary): F[Binary] =
        for
          mac <- E.catchNonFatal(Mac.getInstance("HmacSHA256"))
          _   <- E.catchNonFatal(mac.init(key))
          sig <- E.catchNonFatal(mac.doFinal(data.toArray))
        yield Binary(sig)
    }

  def verifier[F[_]](key: SecretKey)(using E: MonadError[F, Throwable]): Verifier[F] =
    new Verifier[F] {
      type Repr = HS256.type
      def algo: Repr = HS256

      def verify(signature: Binary, data: Binary): F[Boolean] = {
        for
          mac <- E.catchNonFatal(Mac.getInstance("HmacSHA256"))
          _   <- E.catchNonFatal(mac.init(key))
          sig <- E.catchNonFatal(mac.doFinal(data.toArray))
          v   <- E.catchNonFatal(MessageDigest.isEqual(sig, signature.toArray))
        yield v
      }
    }
end HS256

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
