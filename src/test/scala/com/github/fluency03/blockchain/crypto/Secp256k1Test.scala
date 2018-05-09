package com.github.fluency03.blockchain
package crypto

import java.security.KeyPair

import com.github.fluency03.blockchain.crypto.Secp256k1._
import org.scalatest.{FlatSpec, Matchers}

class Secp256k1Test extends FlatSpec with Matchers {

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

    Secp256k1.publicKeyHexToAddress("04B4D653FCBB4B96000C99343F23B08A44FA306031E0587F9E657AB" +
      "4A2541129368D7D9BB05CD8AFBDF7705A6540D98028236965553F91BF1C5B4F70073F55B55D") shouldEqual
      "1DU8Hi1sbHTpEP9vViBEkEw6noeUrgKkJH"

    Secp256k1.publicKeyToAddress(recoverPublicKey("04B4D653FCBB4B96000C99343F23B08A44FA306031E0587F9E657AB" +
      "4A2541129368D7D9BB05CD8AFBDF7705A6540D98028236965553F91BF1C5B4F70073F55B55D")) shouldEqual
      "1DU8Hi1sbHTpEP9vViBEkEw6noeUrgKkJH"

    recoverPublicKey("04B4D653FCBB4B96000C99343F23B08A44FA306031E0587F9E657AB" +
      "4A2541129368D7D9BB05CD8AFBDF7705A6540D98028236965553F91BF1C5B4F70073F55B55D").address shouldEqual
      "1DU8Hi1sbHTpEP9vViBEkEw6noeUrgKkJH"

    Secp256k1.addressToHash160("1DU8Hi1sbHTpEP9vViBEkEw6noeUrgKkJH") shouldEqual
      ("00", "88C2D2FA846282C870A76CADECBE45C4ACD72BB6".toLowerCase)

    Secp256k1.privateKeyToBytes(pair.getPrivate).toBigInt shouldEqual Secp256k1.privateKeyToHex(pair.getPrivate).hex2BigInt

  }


}
