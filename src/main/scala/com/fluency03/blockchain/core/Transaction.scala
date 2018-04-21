package com.fluency03.blockchain.core

import java.security.KeyPair

import com.fluency03.blockchain.Crypto
import com.fluency03.blockchain.Util.hashOf
import com.fluency03.blockchain.core.Transaction.hashOfTransaction
import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods.{compact, render}
import org.json4s.{Extraction, JValue}

import scala.collection.mutable

case class Outpoint(id: String, index: Int)
case class TxIn(previousOut: Outpoint, signature: String)
case class TxOut(address: String, amount: Long)

case class Transaction(txIns: Seq[TxIn], txOuts: Seq[TxOut], timestamp: Long) {
  lazy val id: String = hashOfTransaction(this)

  def addTxIn(in: TxIn): Transaction = Transaction(in +: txIns, txOuts, timestamp)

  def addTxIns(ins: Seq[TxIn]): Transaction = Transaction(ins ++ txIns, txOuts, timestamp)

  def addTxOut(out: TxOut): Transaction = Transaction(txIns, out +: txOuts, timestamp)

  def addTxOuts(outs: Seq[TxOut]): Transaction = Transaction(txIns, outs ++ txOuts, timestamp)

  def removeTxIn(txIn: TxIn): Transaction = Transaction(txIns.filter(_ != txIn), txOuts, timestamp)

  def removeTxOut(txOut: TxOut): Transaction = Transaction(txIns, txOuts.filter(_ != txOut), timestamp)

  def toJson: JValue = Extraction.decompose(this).asInstanceOf[JObject] ~ ("id" -> id)

  override def toString: String = compact(render(toJson))
}

object Transaction {

  lazy val COINBASE_AMOUNT: Int = 50

  def createCoinbase(blockIndex: Int): TxIn = TxIn(Outpoint("", blockIndex), "")

  def createCoinbaseTx(blockIndex: Int, miner: String, timestamp: Long): Transaction = {
    val txIn = createCoinbase(blockIndex)
    val txOut = TxOut(miner, COINBASE_AMOUNT)
    Transaction(Seq(txIn), Seq(txOut), timestamp)
  }

  def hashOfTransaction(tx: Transaction): String =
    hashOf(tx.txIns.map(tx => tx.previousOut.id + tx.previousOut.index).mkString,
      tx.txOuts.map(tx => tx.address + tx.amount).mkString, tx.timestamp.toString)

  def signTxIn(
      transactionHash: Array[Byte],
      txIn: TxIn,
      keyPair: KeyPair,
      unspentTxOuts: mutable.Map[Outpoint, TxOut]
  ): Option[TxIn] = unspentTxOuts.get(txIn.previousOut) match {
    case Some(uTxO) =>
      if (keyPair.getPublic.getEncoded.mkString != uTxO.address) None
      else Some(TxIn(txIn.previousOut, Crypto.sign(transactionHash, keyPair.getPrivate.getEncoded).mkString))
    case None => None
  }

  def validateTxIn(txIn: TxIn, transaction: Transaction, unspentTxOuts: mutable.Map[Outpoint, TxOut]): Boolean =
    unspentTxOuts.get(txIn.previousOut) match {
      case Some(txOut) =>
        Crypto.verify(transaction.id.getBytes(defaultCharset),
          txOut.address.getBytes(defaultCharset),
          txIn.signature.getBytes(defaultCharset))
      case None => false
    }

  def validateTxOutValues(transaction: Transaction, unspentTxOuts: mutable.Map[Outpoint, TxOut]): Boolean = {
    val totalTxInValues: Long = transaction.txIns
      .map(txIn => unspentTxOuts.get(txIn.previousOut) match {
        case Some(txOut) => txOut.amount
        case None => 0
      }).sum

    val totalTxOutValues: Long = transaction.txOuts.map( _.amount).sum

    totalTxInValues == totalTxOutValues
  }



}
