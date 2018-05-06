package com.github.fluency03.blockchain

import org.bouncycastle.crypto.digests.RIPEMD160Digest

object RIPEMD160 {

  def hash(str: String): String = hash(str.getBytes)

  def hash(bytes: Bytes): String = hashToDigest(bytes).map("%02x".format(_)).mkString

  def hashToDigest(bytes: Bytes): Bytes = {
    val (raw, messageDigest) = (bytes, new RIPEMD160Digest())
    messageDigest.update(raw, 0, raw.length)
    val out = Array.fill[Byte](messageDigest.getDigestSize)(0)
    messageDigest.doFinal(out, 0)
    out
  }

  def doubleHash(bytes: Bytes): String = hash(SHA256.hashToDigest(bytes))

  def doubleHashToDigest(bytes: Bytes): Bytes = hashToDigest(SHA256.hashToDigest(bytes))


}
