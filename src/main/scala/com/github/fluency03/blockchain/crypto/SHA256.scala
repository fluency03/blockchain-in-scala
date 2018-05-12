package com.github.fluency03.blockchain
package crypto

import java.security.MessageDigest

object SHA256 {

  /**
   * Generate SHA256 Hash from a input String.
   * https://gist.github.com/navicore/6234040bbfce3aa58f866db314c07c15
   */
  def hash(text: String) : String = hash(text.getBytes)

  /**
   * Generate SHA256 Hash from a input Array of Byte.
   */
  def hash(bytes: Bytes) : String = String.format("%064x",
    new java.math.BigInteger(1, hashToDigest(bytes)))

  /**
   * Generate digest from a input Array of Byte.
   */
  def hashToDigest(bytes: Bytes): Bytes =
    MessageDigest.getInstance("SHA-256").digest(bytes)

  /**
   * Calculate the hash of concatenation a Seq of Strings.
   */
  def hashAll(strings: String*): String = hash(strings mkString "")

  def hash256(bytes: Bytes): String = hash(hashToDigest(bytes))

  def hash256ToDigest(bytes: Bytes): Bytes = hashToDigest(hashToDigest(bytes))

}
