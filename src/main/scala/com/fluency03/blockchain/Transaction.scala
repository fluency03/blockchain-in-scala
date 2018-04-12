package com.fluency03.blockchain

import com.fluency03.blockchain.Transaction.hashOfTransaction
import com.fluency03.blockchain.Util.hashOf
import org.json4s.{Extraction, JValue}
import org.json4s.native.JsonMethods.{compact, render}

case class Transaction(sender: String, receiver: String, amount: Double) {

  lazy val hash: String = hashOfTransaction(this)

  def toJson: JValue = Extraction.decompose(this)

  override def toString: String = compact(render(toJson))

}



object Transaction {

  def hashOfTransaction(tx: Transaction): String =
    hashOfTransactionFields(tx.sender, tx.receiver, tx.amount)

  def hashOfTransactionFields(sender: String, receiver: String, amount: Double): String =
    hashOf(sender, receiver, amount.toString)

}
