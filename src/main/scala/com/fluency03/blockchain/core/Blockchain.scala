package com.fluency03.blockchain.core

import com.fluency03.blockchain.Util.getCurrentTimestamp
import com.fluency03.blockchain.core.Blockchain._

import scala.collection.mutable

/**
 * Blockchain with difficulty and the chain of Blocks.
 * @param difficulty Difficulty of a Blockchain
 * @param chain Chain of Blocks
 */
case class Blockchain(difficulty: Int = 4, chain: List[Block] = List(Block.genesisBlock)) {
  val currentTransactions: mutable.Map[String, Transaction] = mutable.Map.empty[String, Transaction]

  def addBlock(newBlockData: String): Blockchain = {
    Blockchain(difficulty, mineNextBlock(newBlockData).addTransactions(currentTransactions.values.toList) :: chain)
  }

  def addBlock(newBlock: Block): Blockchain = {
    Blockchain(difficulty, newBlock :: chain)
  }

  def addTransaction(tx: Transaction): Blockchain = {
    currentTransactions += (tx.hash -> tx)
    this
  }

  def addTransaction(sender: String, receiver: String, amount: Double): Blockchain =
    addTransaction(Transaction(sender, receiver, amount))

  def addTransaction(sender: String, receiver: String, amount: Double, timestamp: Long): Blockchain =
    addTransaction(Transaction(sender, receiver, amount, timestamp))

  def addTransactions(trans: List[Transaction]): Blockchain = {
    currentTransactions ++= trans.map(tx => (tx.hash, tx))
    this
  }

  def lastBlock(): Option[Block] = chain.headOption

  def mineNextBlock(newBlockData: String): Block = {
    val lastBlockOpt: Option[Block] = this.lastBlock()
    if (lastBlockOpt.isEmpty) throw new NoSuchElementException("Last Block does not exist!")
    val lastHeader = lastBlockOpt.get.header
    Block.mineNextBlock(
        lastHeader.index + 1,
        lastHeader.hash,
        newBlockData,
        currentTransactions.values.toList,
        getCurrentTimestamp,
        difficulty)
  }

  def isValid: Boolean = chain match {
    case Nil => throw new NoSuchElementException("Blockchain is Empty!")
    case _ => isValidChain(chain, difficulty)
  }

}

object Blockchain {

  def apply(difficulty: Int): Blockchain = new Blockchain(difficulty, List(Block.genesis(difficulty)))

  def isValidChain(chain: List[Block], difficulty: Int): Boolean = chain match {
    case Nil => true
    case g :: Nil => g.previousHash == ZERO64 && g.isValid(difficulty)
    case a :: b :: tail => a.previousHash == b.hash && a.isValid(difficulty) && isValidChain(b :: tail, difficulty)
  }

}

