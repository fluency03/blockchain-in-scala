package com.github.fluency03.blockchain
package crypto

import org.bouncycastle.util.encoders.Hex

import scala.annotation.tailrec

object Base58 {

  lazy val ALPHABET: Array[Char] = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray
  lazy val ENCODED_ZERO = ALPHABET(0)

  def encodeString(text: String): Base58 = encode(text.getBytes)

  def encodeHex(hex: HexString): Base58 = encode(hex.hex2Bytes)

  def encode(bytes: Bytes): Base58 = {
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

  def decode(str: Base58): Bytes = {
    @tailrec
    def restoreBigInt(chars: Array[Char], bi: BigInt, idx: Int): BigInt =
      if (idx >= chars.length) bi
      else {
        val i: Int = ALPHABET.zipWithIndex.find(t => t._1 == chars(idx)).map(_._2).get
        restoreBigInt(chars, bi * 58 + i, idx + 1)
      }

    val zeroes = str.takeWhile(_ == '1').map(_ => 0: Byte).toArray
    val trim = str.dropWhile(_ == '1').toCharArray

    val bi = restoreBigInt(trim, 0, 0)
    zeroes ++ Hex.decode(bi.toString(16))
  }

  def decodeToHex(str: Base58): HexString = new String(decode(str))

  def checkEncode(bytes: Bytes): Base58 =
    encode(bytes ++ bytes.sha256Digest.sha256Digest.slice(0, 4))


}