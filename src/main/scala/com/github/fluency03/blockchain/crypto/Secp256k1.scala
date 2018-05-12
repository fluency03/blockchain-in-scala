package com.github.fluency03.blockchain
package crypto

import java.math.BigInteger
import java.security._
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}

import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.interfaces.{ECPrivateKey, ECPublicKey}
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.{ECParameterSpec, ECPrivateKeySpec, ECPublicKeySpec}

object Secp256k1 {

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
    KeyFactory.getInstance(KEY_ALGORITHM).generatePublic(
      new ECPublicKeySpec(ecSpec.getG.multiply(privateKey.asInstanceOf[ECPrivateKey].getD), ecSpec))

  def recoverPublicKey(hex: String): PublicKey =
    KeyFactory.getInstance(KEY_ALGORITHM).generatePublic(
      new ECPublicKeySpec(ecSpec.getCurve.decodePoint(hex.hex2Bytes), ecSpec))

  def recoverPrivateKey(hex: String): PrivateKey =
    KeyFactory.getInstance(KEY_ALGORITHM).generatePrivate(
      new ECPrivateKeySpec(new BigInteger(hex, 16), ecSpec))

  def publicKeyToHex(publicKey: PublicKey): HexString = publicKeyToBytes(publicKey).toHex

  def privateKeyToHex(privateKey: PrivateKey): HexString =
    privateKey.asInstanceOf[ECPrivateKey].getD.toString(16)

  def publicKeyToBytes(publicKey: PublicKey): Bytes =
    publicKey.asInstanceOf[ECPublicKey].getQ.getEncoded(false)

  def privateKeyToBytes(privateKey: PrivateKey): Bytes =
    privateKey.asInstanceOf[ECPrivateKey].getD.toByteArray

  def publicKeyHexToAddress(publicKey: HexString, networkBytes: String = "00"): String =
    Base58.checkEncode(networkBytes.hex2Bytes ++ publicKey.hex2Bytes.hash160Digest)

  /**
   * Convert a Public Key to its Base58 Address.
   * 0 - Private ECDSA Key
   * 1 - Public ECDSA Key
   * 2 - SHA-256 hash of 1
   * 3 - RIPEMD-160 Hash of 2
   * 4 - Adding network bytes to 3
   * 5 - SHA-256 hash of 4
   * 6 - SHA-256 hash of 5
   * 7 - First four bytes of 6
   * 8 - Adding 7 at the end of 4
   * 9 - Base58 encoding of 8
   */
  def publicKeyToAddress(publicKey: PublicKey, networkBytes: String = "00"): Base58 =
    Base58.checkEncode(networkBytes.hex2Bytes ++ publicKeyToBytes(publicKey).hash160Digest)

  def hash160ToAddress(hash160: HexString, networkBytes: String = "00"): Base58 =
    Base58.checkEncode(networkBytes.hex2Bytes ++ hash160.hex2Bytes)

  def addressToHash160(address: Base58, networkBytes: String = "00"): (HexString, HexString) = {
    val decoded: Bytes = Base58.decode(address)
    val (preBytes, _) = decoded.splitAt(decoded.length - 4)
    preBytes.toHex.splitAt(2)
  }

}
