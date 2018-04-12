package com.fluency03.blockchain.core

import com.fluency03.blockchain.Util.getCurrentTimestamp
import scala.collection.mutable


/**
 * Blockchain with difficulty and the chain of Blocks.
 * @param difficulty Difficulty of a Blockchain
 * @param chain Chain of Blocks
 */
case class Blockchain(difficulty: Int = 4, chain: List[Block] = List(Block.genesisBlock)) {
  val currentTransactions: mutable.Set[Transaction] = new mutable.HashSet[Transaction]()

  def addBlock(newBlockData: String): Blockchain = {
    Blockchain(difficulty, mineNextBlock(newBlockData).addTransactions(currentTransactions.toList) :: chain)
  }

  def addTransaction(t: Transaction): Blockchain = {
    currentTransactions += t
    this
  }

  def addTransaction(sender: String, receiver: String, amount: Double): Blockchain =
    addTransaction(Transaction(sender, receiver, amount))

  def addTransaction(trans: Set[Transaction]): Blockchain = {
    currentTransactions ++= trans
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
        MerkleNode.computeRoot(currentTransactions.toList),
        getCurrentTimestamp,
        difficulty)
  }

}

object Blockchain {

  def apply(difficulty: Int): Blockchain = new Blockchain(difficulty)

}

