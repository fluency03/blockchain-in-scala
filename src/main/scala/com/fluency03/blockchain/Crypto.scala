package com.fluency03.blockchain

import java.security._
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}

import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECParameterSpec

object Crypto {

  Security.addProvider(new BouncyCastleProvider)

  val SPECP256K1 = "secp256k1"
  val KEY_ALGORITHM = "ECDSA"
  val KEY_PROVIDER = "BC"

  val ecSpec: ECParameterSpec = ECNamedCurveTable.getParameterSpec(SPECP256K1)

  def sign(data: Array[Byte], privateKey: Array[Byte]): Array[Byte] = {
    val keySpec: PKCS8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey)
    val keyFactory: KeyFactory = KeyFactory.getInstance(KEY_ALGORITHM)
    val key: PrivateKey = keyFactory.generatePrivate(keySpec)

    val sig: Signature = Signature.getInstance(KEY_ALGORITHM, KEY_PROVIDER)
    sig.initSign(key, new SecureRandom)
    sig.update(data)
    sig.sign()
  }

  def verify(data: Array[Byte], publicKey: Array[Byte], signature: Array[Byte]): Boolean =  {
    val keySpec: X509EncodedKeySpec = new X509EncodedKeySpec(publicKey)
    val keyFactory: KeyFactory = KeyFactory.getInstance(KEY_ALGORITHM)
    val key: PublicKey = keyFactory.generatePublic(keySpec)

    val sig: Signature = Signature.getInstance(KEY_ALGORITHM, KEY_PROVIDER)
    sig.initVerify(key)
    sig.update(data)
    sig.verify(signature)
  }

  def generateKeyPair(): KeyPair = {
    val gen: KeyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM, KEY_PROVIDER)
    gen.initialize(ecSpec, new SecureRandom)
    gen.generateKeyPair()
  }

}
