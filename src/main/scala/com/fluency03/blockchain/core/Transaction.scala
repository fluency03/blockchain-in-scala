package com.fluency03.blockchain
package core

import java.security.KeyPair

import com.fluency03.blockchain.Crypto.recoverPublicKey
import com.fluency03.blockchain.core.Transaction.{hashOfTransaction, validateTransaction}
import org.json4s.native.JsonMethods.{compact, render}
import org.json4s.{Extraction, JValue}

import scala.collection.mutable

case class Outpoint(id: String, index: Int)
case class TxIn(previousOut: Outpoint, signature: String)
case class TxOut(address: String, amount: Long)

case class Transaction(txIns: Seq[TxIn], txOuts: Seq[TxOut], timestamp: Long, id: String) {

  def addTxIn(in: TxIn): Transaction = Transaction(in +: txIns, txOuts, timestamp)

  def addTxIns(ins: Seq[TxIn]): Transaction = Transaction(ins ++ txIns, txOuts, timestamp)

  def addTxOut(out: TxOut): Transaction = Transaction(txIns, out +: txOuts, timestamp)

  def addTxOuts(outs: Seq[TxOut]): Transaction = Transaction(txIns, outs ++ txOuts, timestamp)

  def removeTxIn(txIn: TxIn): Transaction = Transaction(txIns.filter(_ != txIn), txOuts, timestamp)

  def removeTxOut(txOut: TxOut): Transaction = Transaction(txIns, txOuts.filter(_ != txOut), timestamp)

  def hasValidId: Boolean = id == hashOfTransaction(this)

  def isValid(uTxOs: mutable.Map[Outpoint, TxOut]): Boolean =
    hasValidId && validateTransaction(this, uTxOs)

  def toJson: JValue = Extraction.decompose(this)

  override def toString: String = compact(render(toJson))
}

object Transaction {

  def apply(txIns: Seq[TxIn], txOuts: Seq[TxOut], timestamp: Long): Transaction =
    Transaction(txIns, txOuts, timestamp, hashOfTransaction(txIns, txOuts, timestamp))

  // coinbase
  final val COINBASE_AMOUNT: Int = 50

  def createCoinbase(blockIndex: Int): TxIn = TxIn(Outpoint("", blockIndex), "")

  def createCoinbaseTx(blockIndex: Int, miner: String, timestamp: Long): Transaction =
    Transaction(Seq(createCoinbase(blockIndex)), Seq(TxOut(miner, COINBASE_AMOUNT)), timestamp)

  // hash of transaction
  def hashOfTransaction(tx: Transaction): String = sha256Of(
    tx.txIns.map(tx => tx.previousOut.id + tx.previousOut.index).mkString,
    tx.txOuts.map(tx => tx.address + tx.amount).mkString,
    tx.timestamp.toString)

  def hashOfTransaction(txIns: Seq[TxIn], txOuts: Seq[TxOut], timestamp: Long): String = sha256Of(
    txIns.map(tx => tx.previousOut.id + tx.previousOut.index).mkString,
    txOuts.map(tx => tx.address + tx.amount).mkString,
    timestamp.toString)

  // sign TxIn
  def signTxIn(txId: String, txIn: TxIn, keyPair: KeyPair, uTxOs: mutable.Map[Outpoint, TxOut]): Option[TxIn] =
    signTxIn(txId.hex2Bytes, txIn, keyPair, uTxOs)

  def signTxIn(txId: Bytes, txIn: TxIn, keyPair: KeyPair, uTxOs: mutable.Map[Outpoint, TxOut]): Option[TxIn] =
    uTxOs.get(txIn.previousOut) match {
      case Some(uTxO) => signTxIn(txId, txIn, keyPair, uTxO)
      case None => None
    }

  def signTxIn(txId: Bytes, txIn: TxIn, keyPair: KeyPair, uTxO: TxOut): Option[TxIn] =
    if (keyPair.getPublic.toHex != uTxO.address) None
    else Some(TxIn(txIn.previousOut, Crypto.sign(txId, keyPair.getPrivate.getEncoded).toHex))

  // validate TxIn's signature
  def validateTxIn(txIn: TxIn, txId: String, uTxOs: mutable.Map[Outpoint, TxOut]): Boolean =
    validateTxIn(txIn, txId.hex2Bytes, uTxOs)

  def validateTxIn(txIn: TxIn, txId: Bytes, uTxOs: mutable.Map[Outpoint, TxOut]): Boolean =
    uTxOs.get(txIn.previousOut) match {
      case Some(txOut) => validateTxIn(txId, txOut, txIn)
      case None => false
    }

  def validateTxIn(txId: Bytes, txOut: TxOut, txIn: TxIn): Boolean =
    Crypto.verify(txId, recoverPublicKey(txOut.address).getEncoded, txIn.signature.hex2Bytes)

  // validate TxOut: Sum of TxOuts is equal to the sum of TxIns
  def validateTxOutValues(transaction: Transaction, uTxOs: mutable.Map[Outpoint, TxOut]): Boolean =
    validateTxOutValues(transaction.txIns, transaction.txOuts, uTxOs)

  def validateTxOutValues(txIns: Seq[TxIn], txOuts: Seq[TxOut], uTxOs: mutable.Map[Outpoint, TxOut]): Boolean =
    txIns.map(txIn => uTxOs.get(txIn.previousOut) match {
      case Some(txOut) => txOut.amount
      case None => 0
    }).sum == txOuts.map( _.amount).sum

  /**
   * Validate Transaction:
   *  1. All TxIns are valid, i.e., has valid signature
   *  2. Sum of TxOuts is equal to the sum of TxIns
   */
  def validateTransaction(transaction: Transaction, uTxOs: mutable.Map[Outpoint, TxOut]): Boolean =
    transaction.txIns.forall(txIn => validateTxIn(txIn, transaction.id, uTxOs)) &&
      validateTxOutValues(transaction, uTxOs)

  /**
   * Update UTXOs:
   *  1. Remove all consumed unspent transaction outputs
   *  2. Append all new unspent transaction outputs
   */
  def updateUTxOs(transactions: Seq[Transaction], uTxOs: Map[Outpoint, TxOut]): Map[Outpoint, TxOut] = {
    val consumedTxOuts = getConsumedUTxOs(transactions)
    uTxOs.filterNot {
      case (i, _) => consumedTxOuts.contains(i)
    } ++ getNewUTxOs(transactions)
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
