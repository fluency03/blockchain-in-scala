package com.github.fluency03.blockchain

import org.bouncycastle.util.encoders.Hex

import scala.annotation.tailrec

object Base58 {

  lazy val ALPHABET: Array[Char] = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray
  private val ENCODED_ZERO = ALPHABET(0)

  def encodeString(str: String): String = encode(str.getBytes)

  def encodeHex(hex: String): String = encode(hex.hex2Bytes)

  def encode(bytes: Bytes): String = {
    @tailrec
    def buildBase58(res: String, bi: BigInt): String =
      if (bi <= 0) res
      else buildBase58(ALPHABET((bi % 58).toInt) + res, bi / 58)

    @tailrec
    def confirmZeroByte(res: String, bytes: Array[Byte], idx: Int): String =
      if (bytes(idx) != 0 || idx >= bytes.length) res
      else confirmZeroByte(ENCODED_ZERO + res, bytes, idx + 1)

    confirmZeroByte(buildBase58("", bytes.toBigInt), bytes, 0)
  }

  def decode(str: String): Bytes = {
    @tailrec
    def restoreBigInt(chars: Array[Char], bi: BigInt, idx: Int): BigInt =
      if (idx >= chars.length) bi
      else {
        val i: Int = ALPHABET.zipWithIndex.find(t => t._1 == chars(idx)).map(_._2).get
        restoreBigInt(chars, bi * 58 + i, idx + 1)
      }

    val bi = restoreBigInt(str.toCharArray, 0, 0)
    Hex.decode(bi.toString(16))
  }

  def decodeToHex(str: String): String = new String(decode(str))

  def checkEncodeHex(str: String): String = encodeString(str + str.hex2Bytes.toSha256Digest.toSha256.substring(0, 8))

  def checkEncode(bytes: Bytes): String = encode(bytes ++ bytes.toSha256Digest.toSha256Digest.slice(0, 4))


}