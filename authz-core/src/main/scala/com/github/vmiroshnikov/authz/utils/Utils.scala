package com.github.vmiroshnikov.authz.utils

import java.security.KeyPairGenerator
import java.security.KeyPair
import java.security.KeyFactory

import java.security.PrivateKey
import java.security.PublicKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

object JCAHelper {
    private lazy val kf = KeyFactory.getInstance("RSA")

    def generateRSAKeyPair: KeyPair =
        val rsaGen = KeyPairGenerator.getInstance("RSA")
        rsaGen.initialize(2048)
        rsaGen.generateKeyPair

    def decodeRSAPrivateKey(data: String): PrivateKey = 
        val keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(data))
        kf.generatePrivate(keySpecPKCS8)

    def decodeRSAPublicKey(data: String): PublicKey =
        val keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(data))
        kf.generatePublic(keySpecX509)
}
