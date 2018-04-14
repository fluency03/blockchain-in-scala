package com.fluency03.blockchain.core

import com.fluency03.blockchain.Util.{hashOf, getCurrentTimestamp}
import com.fluency03.blockchain.core.Transaction.hashOfTransaction
import org.json4s.native.JsonMethods.{compact, render}
import org.json4s.{Extraction, JValue}

/**
 * Transaction
 * @param sender Sender of the current Transaction
 * @param receiver Receiver of the current Transaction
 * @param amount Amount of the current Transaction
 * @param timestamp Unix epoch time of the current Transaction
 */
case class Transaction(sender: String, receiver: String, amount: Double, timestamp: Long) {
  lazy val hash: String = hashOfTransaction(this)

  def toJson: JValue = Extraction.decompose(this)

  override def toString: String = compact(render(toJson))
}

object Transaction {

  def apply(sender: String, receiver: String, amount: Double): Transaction =
    Transaction(sender, receiver, amount, getCurrentTimestamp)

  def hashOfTransaction(tx: Transaction): String =
    hashOfTransactionFields(tx.sender, tx.receiver, tx.amount, tx.timestamp)

  def hashOfTransactionFields(sender: String, receiver: String, amount: Double, timestamp: Long): String =
    hashOf(sender, receiver, amount.toString, timestamp.toString)

}
