package com.fluency03.blockchain

import java.security.KeyPair

import org.scalatest.{FlatSpec, Matchers}

class CryptoTest extends FlatSpec with Matchers {

  "Crypto" should "be able to sign a data and verify the signature. " in {
    val pair: KeyPair = Crypto.generateKeyPair()
    val data = "Welcome to Blockchain in Scala!".toCharArray.map(_.toByte)
    val signature = Crypto.sign(data, pair.getPrivate.getEncoded)
    Crypto.verify(data, pair.getPublic.getEncoded, signature) shouldEqual true
  }


}
