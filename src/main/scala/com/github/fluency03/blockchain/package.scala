package com.github.fluency03

import java.nio.charset.Charset
import java.security.{PrivateKey, PublicKey}
import java.time.Instant

import com.github.fluency03.blockchain.crypto.Secp256k1
import com.github.fluency03.blockchain.core.{Peer, PeerSimple, TxIn, TxOut}
import com.github.fluency03.blockchain.crypto.{RIPEMD160, SHA256, Secp256k1}
import org.bouncycastle.util.encoders.{Base64, Hex}
import org.json4s.native.Serialization
import org.json4s.{Formats, NoTypeHints}

import scala.io.Source

package object blockchain {

  type Bytes = Array[Byte]
  type UTXO = (TxIn, TxOut)
  type Hex = String
  type Binary = String
  type Base64 = String
  type Base58 = String

  implicit val formats: AnyRef with Formats = Serialization.formats(NoTypeHints)

  implicit val defaultCharset: Charset = Charset.forName("UTF-8")

  val GENESIS_MINER_PUBLIC_KEY_RESOURCE: String = "public-key"
  val GENESIS_MINER_PRIVATE_KEY_RESOURCE: String = "private-key"
  val GENESIS_MINER_ADDRESS_RESOURCE: String = "address"

  def getResource(re: String): String = Source.fromResource(re).getLines.mkString

  val ZERO64: String = "0000000000000000000000000000000000000000000000000000000000000000"

  val genesisTime: String = "2018-04-11T18:52:01Z"
  val genesisTimestamp: Long = Instant.parse(genesisTime).getEpochSecond

  val genesisMiner: String = getResource(GENESIS_MINER_PUBLIC_KEY_RESOURCE)

  val SLOGAN: String = "Welcome to Blockchain in Scala!"

  implicit class StringImplicit(val str: String) {
    def hex2Long: Long = java.lang.Long.parseLong(str, 16)
    def hex2BigInt: BigInt = BigInt(str, 16)
    def hex2Bytes: Bytes = Hex.decode(str)
    def hex2Binary: String = binaryOfHex(str)
    def toBase64: String = base64Of(str.getBytes)
    def toSha256: String = SHA256.hash(str)
    def toRipemd160: String = RIPEMD160.hash(str)
  }

  implicit class BytesImplicit(val bytes: Bytes) {
    def toHex: String = Hex.toHexString(bytes)
    def toBigInt: BigInt = BigInt(bytes)
    def toBase64: String = base64Of(bytes)
    def toSha256: String = SHA256.hash(bytes)
    def toSha256Digest: Bytes = SHA256.hashToDigest(bytes)
    def toRipemd160: String = RIPEMD160.hash(bytes)
    def toRipemd160ODigest: Bytes = RIPEMD160.hashToDigest(bytes)
    def toHash160: String = RIPEMD160.hash160(bytes)
    def toHash160Digest: Bytes = RIPEMD160.hash160ToDigest(bytes)
  }

  implicit class PublicKeyImplicit(val publicKey: PublicKey) {
    def toHex: String = Secp256k1.publicKeyToHex(publicKey)
    def toBytes: Bytes = Secp256k1.publicKeyToBytes(publicKey)
    def toHash160: String = Secp256k1.publicKeyToBytes(publicKey).toHash160
    def address: String = Secp256k1.publicKeyToAddress(publicKey)
  }

  implicit class PrivateKeyImplicit(val privateKey: PrivateKey) {
    def toHex: String = Secp256k1.privateKeyToHex(privateKey)
    def toBytes: Bytes = Secp256k1.privateKeyToBytes(privateKey)
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
  def fromBase64(base64: String): String = new String(Base64.decode(base64))

}
