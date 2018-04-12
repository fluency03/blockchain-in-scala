package com.fluency03.blockchain

import java.security.MessageDigest
import java.time.Instant

object Util {

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
  def digestOf(text: String): Array[Byte] =
    MessageDigest.getInstance("SHA-256").digest(text.getBytes("UTF-8"))

  /**
   * Return the current timestamp in Unix Epoch Time.
   */
  def getCurrentTimestamp: Long = Instant.now.getEpochSecond

  /**
   * Calculate the hash of concatenation a List of Strings.
   */
  def hashOf(strings: String*): String = sha256HashOf(strings mkString "")

  /**
   * Calculate the hash of a String.
   */
  def hashOf(str: String): String = sha256HashOf(str)

  /**
   * Check whether the given hash is with valid difficulty.
   */
  def isWithValidDifficulty(hash: String, difficulty: Int): Boolean = hash startsWith ("0" * difficulty)


}
