package com.github.fluency03.blockchain
package crypto

import org.bouncycastle.crypto.digests.RIPEMD160Digest

object RIPEMD160 {

  def hashString(text: String): HexString = hash(text.getBytes)

  def hashHex(hex: HexString): HexString = hash(hex.hex2Bytes)

  def hash(bytes: Bytes): HexString = hashDigest(bytes).map("%02x".format(_)).mkString

  def hashDigest(bytes: Bytes): Bytes = {
    val (raw, messageDigest) = (bytes, new RIPEMD160Digest())
    messageDigest.update(raw, 0, raw.length)
    val out = Array.fill[Byte](messageDigest.getDigestSize)(0)
    messageDigest.doFinal(out, 0)
    out
  }

  def hashStrings(strings: String*): HexString =
    hash(strings.map(_.getBytes).foldLeft(Array.empty[Byte])(_ ++ _))

  def hashHexs(hexs: HexString*): HexString =
    hash(hexs.map(_.hex2Bytes).foldLeft(Array.empty[Byte])(_ ++ _))

  def hash160(bytes: Bytes): HexString = hash(SHA256.hashDigest(bytes))

  def hash160Digest(bytes: Bytes): Bytes = hashDigest(SHA256.hashDigest(bytes))

}
