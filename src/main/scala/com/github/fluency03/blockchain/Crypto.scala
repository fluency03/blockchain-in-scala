package com.github.fluency03.blockchain

import java.math.BigInteger
import java.security._
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}

import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.interfaces.{ECPrivateKey, ECPublicKey}
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.{ECParameterSpec, ECPrivateKeySpec, ECPublicKeySpec}

object Crypto {

  Security.addProvider(new BouncyCastleProvider)

  val SPECP256K1 = "secp256k1"
  val KEY_ALGORITHM = "ECDSA"
  val KEY_PROVIDER = "BC"

  val ecSpec: ECParameterSpec = ECNamedCurveTable.getParameterSpec(SPECP256K1)

  def sign(data: Bytes, privateKey: Bytes): Bytes = {
    val keySpec: PKCS8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey)
    val keyFactory: KeyFactory = KeyFactory.getInstance(KEY_ALGORITHM)
    val key: PrivateKey = keyFactory.generatePrivate(keySpec)

    val sig: Signature = Signature.getInstance(KEY_ALGORITHM, KEY_PROVIDER)
    sig.initSign(key, new SecureRandom)
    sig.update(data)
    sig.sign()
  }

  def verify(data: Bytes, publicKey: Bytes, signature: Bytes): Boolean =  {
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

  def generatePublicKey(privateKey: PrivateKey): PublicKey =
    KeyFactory.getInstance(KEY_ALGORITHM)
      .generatePublic(new ECPublicKeySpec(ecSpec.getG.multiply(privateKey.asInstanceOf[ECPrivateKey].getD), ecSpec))

  def recoverPublicKey(hex: String): PublicKey =
    KeyFactory.getInstance(KEY_ALGORITHM)
      .generatePublic(new ECPublicKeySpec(ecSpec.getCurve.decodePoint(hex.hex2Bytes), ecSpec))

  def recoverPrivateKey(hex: String): PrivateKey =
    KeyFactory.getInstance(KEY_ALGORITHM)
      .generatePrivate(new ECPrivateKeySpec(new BigInteger(hex, 16), ecSpec))

  def publicKeyToHex(publicKey: PublicKey): String = publicKeyToBytes(publicKey).toHex

  def privateKeyToHex(privateKey: PrivateKey): String =
    privateKey.asInstanceOf[ECPrivateKey].getD.toString(16)

  def publicKeyToBytes(publicKey: PublicKey): Bytes =
    publicKey.asInstanceOf[ECPublicKey].getQ.getEncoded(false)

  def privateKeyToBytes(privateKey: PrivateKey): Bytes =
    privateKey.asInstanceOf[ECPrivateKey].getD.toByteArray

  def publicKeyToAddress(publicKey: String, networkBytes: String = "00"): String =
    Base58.checkEncode(networkBytes.hex2Bytes ++ publicKey.hex2Bytes.toHash160Digest)




}
