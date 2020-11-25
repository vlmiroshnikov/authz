package com.github.vmiroshnikov.authz.jwt.impl

import java.security.{PrivateKey, PublicKey, Signature, MessageDigest}
import javax.crypto.{KeyGenerator, Mac, SecretKey}

import cats.effect.Sync
import cats.implicits._
import com.github.vmiroshnikov.authz.jwt._


object HS256 extends Alg(name = "HS256"): 
    def signer[F[_]](key: SecretKey)(using S: Sync[F]): Signer[F] =  
        new Signer[F] {
            type Repr = HS256.type
            def algo: Repr = HS256
        
            def sign(data: Binary): F[Binary] = 
                for
                   mac <- S.delay(Mac.getInstance("HmacSHA256"))
                    _  <- S.delay(mac.init(key))
                   sig <- S.delay(mac.doFinal(data.toArray))
                yield Binary(sig)
        }

    def verifier[F[_]](key: SecretKey)(using S: Sync[F]): Verifier[F] =
        new Verifier[F] {
            type Repr = HS256.type
            def algo: Repr = HS256
        
            def verify(signature: Binary, data: Binary): F[Boolean] = {
                for 
                   mac <- S.delay(Mac.getInstance("HmacSHA256"))
                    _  <- S.delay(mac.init(key))
                   sig <- S.delay(mac.doFinal(data.toArray))
                   v   <- S.delay(MessageDigest.isEqual(sig, signature.toArray))                     
                yield v
            }
        }
end HS256

object RS256 extends Alg(name = "RS256"): 
    
    def signer[F[_]](key: PrivateKey)(using S: Sync[F]): Signer[F] =  
        new Signer[F] {
            type Repr = RS256.type
            def algo: Repr = RS256
        
            def sign(data: Binary): F[Binary] = 
                for
                   rsa  <- S.delay(Signature.getInstance("SHA256withRSA"))
                    _   <- S.delay(rsa.initSign(key))
                    _   <- S.delay(rsa.update(data.toArray))
                    sig <- S.delay(rsa.sign())    
                yield Binary(sig)
        }

    def verifier[F[_]](key: PublicKey)(using S: Sync[F]): Verifier[F] =
        new Verifier[F] {
            type Repr = RS256.type
            def algo: Repr = RS256
        
            def verify(signature: Binary, data: Binary): F[Boolean] = {
                for 
                   rsa <- S.delay(Signature.getInstance("SHA256withRSA"))
                   _   <- S.delay(rsa.initVerify(key))
                   _   <- S.delay(rsa.update(data.toArray))
                   v   <- S.delay(rsa.verify(signature.toArray))                     
                yield v
            }
        }
        
end RS256

