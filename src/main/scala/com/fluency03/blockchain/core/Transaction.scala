package com.fluency03.blockchain
package core

import java.security.KeyPair

import com.fluency03.blockchain.Crypto
import com.fluency03.blockchain.core.Transaction.hashOfTransaction
import org.bouncycastle.util.encoders.Hex
import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods.{compact, render}
import org.json4s.{Extraction, JValue}

import scala.collection.mutable

case class Outpoint(id: String, index: Int)
case class TxIn(previousOut: Outpoint, signature: String)
case class TxOut(address: String, amount: Long)

case class Transaction(txIns: Seq[TxIn], txOuts: Seq[TxOut], timestamp: Long, id: String) {
//  lazy val id: String = hashOfTransaction(this)

  def addTxIn(in: TxIn): Transaction = Transaction(in +: txIns, txOuts, timestamp)

  def addTxIns(ins: Seq[TxIn]): Transaction = Transaction(ins ++ txIns, txOuts, timestamp)

  def addTxOut(out: TxOut): Transaction = Transaction(txIns, out +: txOuts, timestamp)

  def addTxOuts(outs: Seq[TxOut]): Transaction = Transaction(txIns, outs ++ txOuts, timestamp)

  def removeTxIn(txIn: TxIn): Transaction = Transaction(txIns.filter(_ != txIn), txOuts, timestamp)

  def removeTxOut(txOut: TxOut): Transaction = Transaction(txIns, txOuts.filter(_ != txOut), timestamp)

  def toJson: JValue = Extraction.decompose(this)

  override def toString: String = compact(render(toJson))
}

object Transaction {

  def apply(txIns: Seq[TxIn], txOuts: Seq[TxOut], timestamp: Long): Transaction =
    Transaction(txIns, txOuts, timestamp, hashOfTransaction(txIns, txOuts, timestamp))

  lazy val COINBASE_AMOUNT: Int = 50

  def createCoinbase(blockIndex: Int): TxIn = TxIn(Outpoint("", blockIndex), "")

  def createCoinbaseTx(blockIndex: Int, miner: String, timestamp: Long): Transaction = {
    val txIn = createCoinbase(blockIndex)
    val txOut = TxOut(miner, COINBASE_AMOUNT)
    Transaction(Seq(txIn), Seq(txOut), timestamp)
  }

  def hashOfTransaction(tx: Transaction): String =
    sha256Of(tx.txIns.map(tx => tx.previousOut.id + tx.previousOut.index).mkString,
      tx.txOuts.map(tx => tx.address + tx.amount).mkString, tx.timestamp.toString)

  def hashOfTransaction(txIns: Seq[TxIn], txOuts: Seq[TxOut], timestamp: Long): String =
    sha256Of(txIns.map(tx => tx.previousOut.id + tx.previousOut.index).mkString,
      txOuts.map(tx => tx.address + tx.amount).mkString, timestamp.toString)

  def signTxIn(txId: String, txIn: TxIn, keyPair: KeyPair, unspentTxOuts: mutable.Map[Outpoint, TxOut]): Option[TxIn] =
    signTxIn(txId.hex2Bytes, txIn, keyPair, unspentTxOuts)

  def signTxIn(txId: Bytes, txIn: TxIn, keyPair: KeyPair, unspentTxOuts: mutable.Map[Outpoint, TxOut]): Option[TxIn] =
    unspentTxOuts.get(txIn.previousOut) match {
      case Some(uTxO) =>
        if (keyPair.getPublic.getEncoded.toHex != uTxO.address) None
        else Some(TxIn(txIn.previousOut, Crypto.sign(txId, keyPair.getPrivate.getEncoded).toHex))
      case None => None
    }

  def validateTxIn(txIn: TxIn, txId: String, unspentTxOuts: mutable.Map[Outpoint, TxOut]): Boolean =
    validateTxIn(txIn, txId.hex2Bytes, unspentTxOuts)

  def validateTxIn(txIn: TxIn, txId: Bytes, unspentTxOuts: mutable.Map[Outpoint, TxOut]): Boolean =
    unspentTxOuts.get(txIn.previousOut) match {
      case Some(txOut) => Crypto.verify(txId, txOut.address.hex2Bytes, txIn.signature.hex2Bytes)
      case None => false
    }

  def validateTxOutValues(transaction: Transaction, unspentTxOuts: mutable.Map[Outpoint, TxOut]): Boolean =
    validateTxOutValues(transaction.txIns, transaction.txOuts, unspentTxOuts)

  def validateTxOutValues(txIns: Seq[TxIn], txOuts: Seq[TxOut], unspentTxOuts: mutable.Map[Outpoint, TxOut]): Boolean = {
    val totalTxInValues: Long = txIns
      .map(txIn => unspentTxOuts.get(txIn.previousOut) match {
        case Some(txOut) => txOut.amount
        case None => 0
      }).sum

    val totalTxOutValues: Long = txOuts.map( _.amount).sum

    totalTxInValues == totalTxOutValues
  }

  def validateTransaction(transaction: Transaction, unspentTxOuts: mutable.Map[Outpoint, TxOut]): Boolean =
    transaction.txIns.forall(txIn => validateTxIn(txIn, transaction.id, unspentTxOuts)) &&
      validateTxOutValues(transaction, unspentTxOuts)

  def updateUTxOs(transactions: Seq[Transaction], unspentTxOuts: Map[Outpoint, TxOut]): Map[Outpoint, TxOut] = {
    val newUnspentTxOuts = getNewUTxOs(transactions)
    val consumedTxOuts = getConsumedUTxOs(transactions)
    unspentTxOuts.filterNot {
      case (i, _) => consumedTxOuts.contains(i)
    } ++ newUnspentTxOuts
  }

  def getNewUTxOs(transactions: Seq[Transaction]): Map[Outpoint, TxOut] =
    transactions
      .map(t => t.txOuts.zipWithIndex.map {
        case (txOut, index) => Outpoint(t.id, index) -> txOut
      }.toMap)
      .reduce { _ ++ _ }

  def getConsumedUTxOs(transactions: Seq[Transaction]): Map[Outpoint, TxOut] =
    transactions.map(_.txIns)
      .reduce { _ ++ _ }
      .map(txIn => Outpoint(txIn.previousOut.id, txIn.previousOut.index) -> TxOut("", 0))
      .toMap

}
