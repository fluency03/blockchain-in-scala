package com.fluency03.blockchain
package core

import com.fluency03.blockchain.core.BlockHeader.hashOfHeaderFields
import com.fluency03.blockchain.core.Transaction.createCoinbaseTx
import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods.{compact, render}
import org.json4s.{Extraction, JValue}

/**
 * Block on the chain.
 * @param header Header of current Block
 * @param transactions Seq of Transactions included in current Block
 */
case class Block(header: BlockHeader, transactions: Seq[Transaction] = Seq()) {
  lazy val index: Int = header.index
  lazy val previousHash: String = header.previousHash
  lazy val data: String = header.data
  lazy val merkleHash: String = header.merkleHash
  lazy val timestamp: Long = header.timestamp
  lazy val difficulty: Int = header.difficulty
  lazy val nonce: Int = header.nonce

  lazy val hash: String = header.hash

  def nextTrial(): Block = Block(header.nextTrial(), transactions)

  def addTransaction(tx: Transaction): Block =
    Block(index, previousHash, data, timestamp, difficulty, nonce, tx +: transactions)

  def addTransactions(trans: Seq[Transaction]): Block =
    Block(index, previousHash, data, timestamp, difficulty, nonce, trans ++ transactions)

  def removeTransaction(tx: Transaction): Block =
    Block(index, previousHash, data, timestamp, difficulty, nonce, transactions.filter(_ != tx))

  def isValid: Boolean = isWithValidDifficulty(hash, difficulty) && hasValidMerkleHash

  def hasValidMerkleHash: Boolean = merkleHash == Merkle.computeRoot(transactions)

  def toJson: JValue = ("header" -> header.toJson) ~ ("transactions" -> transactions.map(_.toJson)) ~ ("hash" -> hash)

  override def toString: String = compact(render(toJson))
}

object Block {

  def apply(
      index: Int,
      previousHash: String,
      data: String,
      merkleHash: String,
      timestamp: Long,
      difficulty: Int,
      nonce: Int): Block =
    Block(BlockHeader(index, previousHash, data, merkleHash, timestamp, difficulty, nonce))

  def apply(
      index: Int,
      previousHash: String,
      data: String,
      timestamp: Long,
      difficulty: Int,
      nonce: Int,
      transactions: Seq[Transaction]): Block =
    Block(BlockHeader(index, previousHash, data, Merkle.computeRoot(transactions), timestamp, difficulty, nonce),
      transactions)

  def apply(
      index: Int,
      previousHash: String,
      data: String,
      merkleHash: String,
      timestamp: Long,
      difficulty: Int,
      nonce: Int,
      transactions: Seq[Transaction]): Block =
    Block(BlockHeader(index, previousHash, data, merkleHash, timestamp, difficulty, nonce), transactions)

  lazy val genesisBlock: Block = genesis()

  def genesis(difficulty: Int = 4): Block =
    mineNextBlock(0, ZERO64, "Welcome to Blockchain in Scala!", genesisTimestamp, difficulty,
      Seq(createCoinbaseTx(0, genesisMiner, genesisTimestamp)))

  def mineNextBlock(
      nextIndex: Int,
      prevHash: String,
      newBlockData: String,
      timestamp: Long,
      difficulty: Int,
      transactions: Seq[Transaction]): Block = {
    var nonce = 0
    var nextHash = ""
    val merkleHash = Merkle.computeRoot(transactions)

    while (!isWithValidDifficulty(nextHash, difficulty)) {
      nonce += 1
      nextHash = hashOfHeaderFields(nextIndex, prevHash, newBlockData, merkleHash, timestamp, difficulty, nonce)
    }

    Block(nextIndex, prevHash, newBlockData, merkleHash, timestamp, difficulty, nonce, transactions)
  }

  def mineNextBlock(
      currentBlock: Block,
      newBlockData: String,
      timestamp: Long,
      difficulty: Int,
      transactions: Seq[Transaction]): Block =
    mineNextBlock(currentBlock.index + 1, currentBlock.hash, newBlockData, timestamp, difficulty, transactions)

  def canBeChained(newBlock: Block, previousBlock: Block): Boolean =
    previousBlock.index + 1 == newBlock.index && previousBlock.hash == newBlock.previousHash



}

