package com.github.fluency03.blockchain
package crypto

import java.security.MessageDigest

object SHA256 {

  /**
   * Generate SHA256 Hash from a input String.
   * https://gist.github.com/navicore/6234040bbfce3aa58f866db314c07c15
   */
  def hashString(text: String) : HexString = hash(text.getBytes)

  def hashHex(hex: HexString) : HexString = hash(hex.hex2Bytes)

  def hash(bytes: Bytes) : HexString = String.format("%064x",
    new java.math.BigInteger(1, hashDigest(bytes)))

  def hashDigest(bytes: Bytes): Bytes =
    MessageDigest.getInstance("SHA-256").digest(bytes)

  def hashStrings(strings: String*): HexString =
    hash(strings.map(_.getBytes).foldLeft(Array.empty[Byte])(_ ++ _))

  def hashHexs(hexs: HexString*): HexString =
    hash(hexs.map(_.hex2Bytes).foldLeft(Array.empty[Byte])(_ ++ _))

  def hash256(bytes: Bytes): HexString = hash(hashDigest(bytes))

  def hash256Digest(bytes: Bytes): Bytes = hashDigest(hashDigest(bytes))

}
