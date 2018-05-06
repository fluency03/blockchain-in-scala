package com.github.fluency03

import java.nio.charset.Charset
import java.security.{PrivateKey, PublicKey}
import java.time.Instant

import com.github.fluency03.blockchain.Crypto.{privateKeyToHex, publicKeyToHex}
import com.github.fluency03.blockchain.core.{Peer, PeerSimple, TxIn, TxOut}
import org.bouncycastle.crypto.digests.RIPEMD160Digest
import org.bouncycastle.util.encoders.{Base64, Hex}
import org.json4s.native.Serialization
import org.json4s.{Formats, NoTypeHints}

import scala.io.Source

package object blockchain {

  type Bytes = Array[Byte]
  type UTXO = (TxIn, TxOut)

  implicit val formats: AnyRef with Formats = Serialization.formats(NoTypeHints)

  implicit val defaultCharset: Charset = Charset.forName("UTF-8")

  val ZERO64: String = "0000000000000000000000000000000000000000000000000000000000000000"

  lazy val genesisTimestamp: Long = Instant.parse(genesisTime).getEpochSecond

  val genesisTime: String = "2018-04-11T18:52:01Z"

  val genesisMiner: String = Source.fromResource("public-key").getLines.mkString

  val SLOGAN: String = "Welcome to Blockchain in Scala!"

  implicit class StringImplicit(val str: String) {
    def hex2Long: Long = java.lang.Long.parseLong(str, 16)
    def hex2BigInt: BigInt = BigInt(str, 16)
    def hex2Bytes: Array[Byte] = Hex.decode(str)
    def hex2Binary: String = binaryOfHex(str)
    def toBase64: String = base64Of(str.getBytes)
    def toSha256: String = SHA256.hash(str)
    def toRipemd160Of: String = ripemd160Of(str)
  }

  implicit class BytesImplicit(val bytes: Bytes) {
    def toHex: String = Hex.toHexString(bytes)
    def toBigInt: BigInt = BigInt(bytes)
    def toBase64: String = base64Of(bytes)
    def toSha256: String = SHA256.hash(bytes)
    def toSha256Digest: Bytes = SHA256.hashToDigest(bytes)
    def toRipemd160Of: String = ripemd160Of(bytes)
    def toRipemd160ODigest: Bytes = ripemd160ODigestOf(bytes)
    def toHash160: String = hash160Of(bytes)
    def toHash160Bytes: Bytes = hash160BytesOf(bytes)
  }

  implicit class PublicKeyImplicit(val publicKey: PublicKey) {
    def toHex: String = publicKeyToHex(publicKey)
  }

  implicit class PrivateKeyImplicit(val privateKey: PrivateKey) {
    def toHex: String = privateKeyToHex(privateKey)
  }

  implicit class PeerImplicit(val peer: Peer) {
    def toSimple: PeerSimple = PeerSimple(peer.name)
  }


  /**
   * Return the current timestamp in Unix Epoch Time.
   */
  def getCurrentTimestamp: Long = Instant.now.getEpochSecond

  /**
   * Parse a time format string to its Epoch time in seconds.
   */
  def epochTimeOf(t: String): Long = Instant.parse(t).getEpochSecond

  /**
   * Get binary representation of a hash.
   */
  def binaryOfHex(hash: String): String = BigInt(hash, 16).toString(2)

  /**
   * Check whether the given hash is with valid difficulty.
   */
  def isWithValidDifficulty(hash: String, difficulty: Int): Boolean = hash startsWith ("0" * difficulty)

  /**
   * Encode a String to Base64.
   */
  def base64Of(text: String): String = Base64.toBase64String(text.getBytes)

  /**
   * Encode an Array of Bytes String to Base64.
   */
  def base64Of(data: Bytes): String = Base64.toBase64String(data)

  /**
   * Decode a Base64 to String.
   */
  def fromBase64(base64: String): String = new String(Base64.decode(base64), "UTF-8")


  def ripemd160Of(str: String): String = ripemd160Of(str.getBytes)

  def ripemd160Of(bytes: Bytes): String = ripemd160ODigestOf(bytes).map("%02x".format(_)).mkString

  def ripemd160ODigestOf(bytes: Bytes): Bytes = {
    val (raw, messageDigest) = (bytes, new RIPEMD160Digest())
    messageDigest.update(raw, 0, raw.length)
    val out = Array.fill[Byte](messageDigest.getDigestSize)(0)
    messageDigest.doFinal(out, 0)
    out
  }

  def hash160Of(bytes: Bytes): String = ripemd160Of(SHA256.hashToDigest(bytes))

  def hash160BytesOf(bytes: Bytes): Bytes = ripemd160ODigestOf(SHA256.hashToDigest(bytes))



}
