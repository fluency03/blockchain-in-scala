package com.fluency03.blockchain

import java.security.MessageDigest
import java.time.Instant

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import com.fluency03.blockchain.api.{FailureMsg, Message, SuccessMsg}
import org.bouncycastle.util.encoders.Base64

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
  def digestOf(text: String): Bytes =
    MessageDigest.getInstance("SHA-256").digest(text.getBytes("UTF-8"))

  /**
   * Return the current timestamp in Unix Epoch Time.
   */
  def getCurrentTimestamp: Long = Instant.now.getEpochSecond

  /**
   * Parse a time format string to its Epoch time in seconds.
   */
  def epochTimeOf(t: String): Long = Instant.parse(t).getEpochSecond

  /**
   * Calculate the hash of concatenation a Seq of Strings.
   */
  def sha256Of(strings: String*): String = sha256HashOf(strings mkString "")

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
  def base64Of(text: String): String = Base64.toBase64String(text.getBytes("UTF-8"))

  /**
   * Encode an Array of Bytes String to Base64.
   */
  def base64Of(data: Bytes): String = Base64.toBase64String(data)

  /**
   * Decode a Base64 to String.
   */
  def fromBase64(base64: String): String = new String(Base64.decode(base64), "UTF-8")

  /**
   * Return either SuccessMsg (if fun successfully returned a String) or FailureMsg (if fun failed).
   */
  def failsafeMsg(fun: => String): Message =
    try { SuccessMsg(fun) }
    catch {
      case e: Exception => FailureMsg(e.getMessage)
    }

  /**
   * Return either SuccessMsg (if fun successfully returned a String) or FailureMsg (if fun failed).
   */
  def failsafeResp(fun: => String): (StatusCode, Message) =
    try { (StatusCodes.OK, SuccessMsg(fun)) }
    catch {
      case e: Exception => (StatusCodes.InternalServerError, FailureMsg(e.getMessage))
    }

}
