package com.fluency03.blockchain

import com.fluency03.blockchain.Util._
import com.fluency03.blockchain.BlockHeader.hashOfHeaderFields

import scala.collection.mutable

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
    val nextIndex = lastHeader.index + 1
    val prevHash = lastHeader.hash
    val nextTimestamp = getCurrentTimestamp
    var nonce = 0
    var nextHash = ""

    while (!isWithValidDifficulty(nextHash, difficulty)) {
      nonce += 1
      nextHash = hashOfHeaderFields(nextIndex, prevHash, newBlockData, nextTimestamp, nonce)
    }

    Block(nextIndex, prevHash, newBlockData, nextTimestamp, nonce)
  }

}


object Blockchain {

  def apply(difficulty: Int = 4): Blockchain = new Blockchain(difficulty)

}

