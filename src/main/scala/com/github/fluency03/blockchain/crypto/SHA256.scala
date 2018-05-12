package com.github.fluency03.blockchain
package crypto

import java.security.MessageDigest

object SHA256 {

  /**
   * Generate SHA256 Hash from a input String.
   * https://gist.github.com/navicore/6234040bbfce3aa58f866db314c07c15
   */
  def hash(text: String) : HexString = hash(text.getBytes)

  def hash(bytes: Bytes) : HexString = String.format("%064x",
    new java.math.BigInteger(1, hashDigest(bytes)))

  def hashDigest(bytes: Bytes): Bytes =
    MessageDigest.getInstance("SHA-256").digest(bytes)

  def hashAll(strings: String*): HexString = hash(strings mkString "")

  def hash256(bytes: Bytes): HexString = hash(hashDigest(bytes))

  def hash256ToDigest(bytes: Bytes): Bytes = hashDigest(hashDigest(bytes))

}
