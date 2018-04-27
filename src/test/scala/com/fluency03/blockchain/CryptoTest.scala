package com.fluency03.blockchain

import com.fluency03.blockchain.Crypto._

import java.security.KeyPair

import org.bouncycastle.jce.interfaces.{ECPrivateKey, ECPublicKey}
import org.scalatest.{FlatSpec, Matchers}

class CryptoTest extends FlatSpec with Matchers {

  "Crypto" should "be able to sign a data and verify the signature. " in {
    val pair: KeyPair = generateKeyPair()
    val data = "Welcome to Blockchain in Scala!".toCharArray.map(_.toByte)
    val signature = sign(data, pair.getPrivate.getEncoded)
    verify(data, pair.getPublic.getEncoded, signature) shouldEqual true

    generatePublicKey(pair.getPrivate) shouldEqual pair.getPublic
    recoverPublicKey(publicKeyToHex(pair.getPublic)) shouldEqual pair.getPublic
    recoverPrivateKey(privateKeyToHex(pair.getPrivate)) shouldEqual pair.getPrivate
    recoverPublicKey(pair.getPublic.toHex) shouldEqual pair.getPublic
    recoverPrivateKey(pair.getPrivate.toHex) shouldEqual pair.getPrivate
  }


}
