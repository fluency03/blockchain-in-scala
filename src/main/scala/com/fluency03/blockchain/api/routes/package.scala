package com.fluency03.blockchain.api

import akka.http.scaladsl.model.{StatusCode, StatusCodes}

package object routes {

  // generics
  val SLASH = "/"
  val GENERIC = "generic"
  val TO_SHA256 = "to-sha256"
  val TO_BASE64 = "to-base64"
  val FROM_BASE64 = "from-base64"
  val TO_EPOCH_TIME = "to-epoch-time"
  val TIME_FROM_EPOCH = "time-from-epoch"

  // blockchain
  val BLOCKCHAIN = "blockchain"

  // block
  val BLOCKS = "blocks"
  val BLOCK = "block"

  // network
  val PEERS = "peers"
  val PEER = "peer"

  // transaction
  val TRANSACTIONS = "transactions"
  val TRANSACTION = "transaction"

  /**
   * Concatenate a seq of String segments into full API path
   */
  def pathOf(seg: String*): String = SLASH + seg.mkString(SLASH)

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
