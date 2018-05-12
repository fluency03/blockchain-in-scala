package com.github.fluency03

import java.nio.charset.Charset
import java.security.{PrivateKey, PublicKey}
import java.time.Instant

import com.github.fluency03.blockchain.core.{Peer, PeerSimple, TxIn, TxOut}
import com.github.fluency03.blockchain.crypto.{RIPEMD160, SHA256, Secp256k1}
import org.bouncycastle.util.encoders.{Base64, Hex}
import org.json4s.native.Serialization
import org.json4s.{Formats, NoTypeHints}

import scala.io.Source

package object blockchain {

  type Bytes = Array[Byte]
  type UTXO = (TxIn, TxOut)
  type HexString = String
  type BinaryString = String
  type Base64 = String
  type Base58 = String

  implicit val formats: AnyRef with Formats = Serialization.formats(NoTypeHints)

  implicit val defaultCharset: Charset = Charset.forName("UTF-8")

  val GENESIS_MINER_PUBLIC_KEY_RESOURCE: String = "public-key"
  val GENESIS_MINER_PRIVATE_KEY_RESOURCE: String = "private-key"
  val GENESIS_MINER_ADDRESS_RESOURCE: String = "address"

  def getResource(re: String): String = Source.fromResource(re).getLines.mkString

  val ZERO64: HexString = "0000000000000000000000000000000000000000000000000000000000000000"

  val genesisTime: String = "2018-04-11T18:52:01Z"
  val genesisTimestamp: Long = Instant.parse(genesisTime).getEpochSecond

  val genesisMiner: HexString = getResource(GENESIS_MINER_PUBLIC_KEY_RESOURCE)

  val SLOGAN: String = "Welcome to Blockchain in Scala!"

  implicit class StringImplicit(val str: String) {
    def hex2Long: Long = java.lang.Long.parseLong(str, 16)
    def hex2BigInt: BigInt = BigInt(str, 16)
    def hex2Bytes: Bytes = Hex.decode(str)
    def hex2Binary: BinaryString = binaryOfHex(str)
    def toBase64: Base64 = base64Of(str.getBytes)
    def sha256: HexString = SHA256.hash(str)
    def ripemd160: HexString = RIPEMD160.hash(str)
  }

  implicit class BytesImplicit(val bytes: Bytes) {
    def toHex: HexString = Hex.toHexString(bytes)
    def toBigInt: BigInt = BigInt(bytes)
    def toBase64: Base64 = base64Of(bytes)
    def sha256: HexString = SHA256.hash(bytes)
    def sha256Digest: Bytes = SHA256.hashDigest(bytes)
    def ripemd160: HexString = RIPEMD160.hash(bytes)
    def ripemd160ODigest: Bytes = RIPEMD160.hashDigest(bytes)
    def hash160: HexString = RIPEMD160.hash160(bytes)
    def hash160Digest: Bytes = RIPEMD160.hash160Digest(bytes)
  }

  implicit class PublicKeyImplicit(val publicKey: PublicKey) {
    def toHex: HexString = Secp256k1.publicKeyToHex(publicKey)
    def toBytes: Bytes = Secp256k1.publicKeyToBytes(publicKey)
    def hash160: HexString = Secp256k1.publicKeyToBytes(publicKey).hash160
    def address: Base58 = Secp256k1.publicKeyToAddress(publicKey)
  }

  implicit class PrivateKeyImplicit(val privateKey: PrivateKey) {
    def toHex: HexString = Secp256k1.privateKeyToHex(privateKey)
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
  def binaryOfHex(hex: HexString): BinaryString = BigInt(hex, 16).toString(2)

  /**
   * Check whether the given hash is with valid difficulty.
   */
  def isWithValidDifficulty(hash: HexString, difficulty: Int): Boolean =
    hash.startsWith("0" * difficulty)

  /**
   * Encode a String to Base64.
   */
  def base64Of(text: String): Base64 = Base64.toBase64String(text.getBytes)

  /**
   * Encode an Array of Bytes String to Base64.
   */
  def base64Of(data: Bytes): Base64 = Base64.toBase64String(data)

  /**
   * Decode a Base64 to String.
   */
  def fromBase64(base64: Base64): String = new String(Base64.decode(base64))

}
