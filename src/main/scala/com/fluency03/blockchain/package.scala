package com.fluency03

import java.nio.charset.Charset
import java.security.{MessageDigest, PrivateKey, PublicKey}
import java.time.Instant

import com.fluency03.blockchain.Crypto.{privateKeyToHex, publicKeyToHex}
import org.bouncycastle.util.encoders.{Base64, Hex}
import org.json4s.{Formats, NoTypeHints}
import org.json4s.native.Serialization

import scala.io.Source

package object blockchain {

  type Bytes = Array[Byte]

  implicit val formats: AnyRef with Formats = Serialization.formats(NoTypeHints)

  implicit val defaultCharset: Charset = Charset.forName("UTF-8")

  val ZERO64: String = "0000000000000000000000000000000000000000000000000000000000000000"

  lazy val genesisTimestamp: Long = Instant.parse(genesisTime).getEpochSecond

  val genesisTime: String = "2018-04-11T18:52:01Z"

  val genesisMiner: String = Source.fromResource("public-key").getLines.mkString

  val SLOGAN: String = "Welcome to Blockchain in Scala!"

  implicit class StringImplicit(val str: String) {
    def hex2Long: Long = java.lang.Long.parseLong(str, 16)
    def hex2Bytes: Array[Byte] = Hex.decode(str)
    def hex2Binary: String = binaryOfHex(str)
    def toBase64: String = base64Of(str.getBytes("UTF-8"))
    def toSha256: String = sha256HashOf(str)
  }

  implicit class BytesImplicit(val bytes: Bytes) {
    def toHex: String = Hex.toHexString(bytes)
    def toBase64: String = base64Of(bytes)
  }

  implicit class PublicKeyImplicit(val publicKey: PublicKey) {
    def toHex: String = publicKeyToHex(publicKey)
  }

  implicit class PrivateKeyImplicit(val privateKey: PrivateKey) {
    def toHex: String = privateKeyToHex(privateKey)
  }


  /**
   * Generate SHA256 Hash from a input String.
   * https://gist.github.com/navicore/6234040bbfce3aa58f866db314c07c15
   */
  def sha256HashOf(text: String) : String = String.format("%064x",
    new java.math.BigInteger(1, digestOf(text)))

  /**
   * Generate digest from a input String.
   * https://gist.github.com/navicore/6234040bbfce3aa58f866db314c07c15
   */
  def digestOf(text: String): Bytes =
    MessageDigest.getInstance("SHA-256").digest(text.getBytes("UTF-8"))

  /**
   * Calculate the hash of concatenation a Seq of Strings.
   */
  def sha256Of(strings: String*): String = sha256HashOf(strings mkString "")

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
  def base64Of(text: String): String = Base64.toBase64String(text.getBytes("UTF-8"))

  /**
   * Encode an Array of Bytes String to Base64.
   */
  def base64Of(data: Bytes): String = Base64.toBase64String(data)

  /**
   * Decode a Base64 to String.
   */
  def fromBase64(base64: String): String = new String(Base64.decode(base64), "UTF-8")



}
