package com.fluency03.blockchain.core

import java.time.Instant

import com.fluency03.blockchain.Util.isWithValidDifficulty
import com.fluency03.blockchain.core.BlockHeader.hashOfHeaderFields
import org.json4s.JValue
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods.{compact, render}

/**
 * Block on the chain.
 * @param header Header of current Block
 * @param transactions List of Transactions included in current Block
 */
case class Block(header: BlockHeader, transactions: List[Transaction] = List()) {
  lazy val index: Int = header.index
  lazy val previousHash: String = header.previousHash
  lazy val data: String = header.data
  lazy val merkleHash: String = header.merkleHash
  lazy val timestamp: Long = header.timestamp
  lazy val nonce: Int = header.nonce

  lazy val hash: String = header.hash

  def nextTrial(): Block = Block(header.nextTrial(), transactions)

  def addTransaction(t: Transaction): Block =
    Block(header, t :: transactions)

  def addTransaction(sender: String, receiver: String, amount: Double): Block =
    addTransaction(Transaction(sender, receiver, amount))

  def addTransactions(trans: List[Transaction]): Block =
    Block(header, trans)

  def isValid(difficulty: Int): Boolean = hasValidHash(difficulty) && hasValidMerkleHash

  def hasValidHash(difficulty: Int): Boolean = header.isValidWith(difficulty)

  def hasValidMerkleHash: Boolean = merkleHash == MerkleNode.computeRoot(transactions)

  def toJson: JValue =
      ("header" -> header.toJson) ~
      ("hash" -> hash) ~
      ("transactions" -> transactions.map(_.toJson))

  override def toString: String = compact(render(toJson))

}

object Block {

  def apply(index: Int, previousHash: String, data: String, merkleHash: String, timestamp: Long, nonce: Int): Block =
    Block(BlockHeader(index, previousHash, data, merkleHash, timestamp, nonce))

  def apply(index: Int, previousHash: String, data: String, transactions: List[Transaction], timestamp: Long, nonce: Int): Block ={
    val merkleHash = MerkleNode.computeRoot(transactions)
    Block(BlockHeader(index, previousHash, data, merkleHash, timestamp, nonce), transactions)
  }

  def apply(index: Int, previousHash: String, data: String, merkleHash: String, transactions: List[Transaction], timestamp: Long, nonce: Int): Block =
    Block(BlockHeader(index, previousHash, data, merkleHash, timestamp, nonce), transactions)

  lazy val genesisBlock: Block = genesis()

  def genesis(): Block =
    mineNextBlock(
        0,
        ZERO64,
        "Welcome to Blockchain in Scala!",
        List(Transaction(ZERO64, ZERO64, 50)),
        Instant.parse("2018-04-11T18:52:01Z").getEpochSecond,
        4)

  def mineNextBlock(
      nextIndex: Int,
      prevHash: String,
      newBlockData: String,
      transactions: List[Transaction],
      timestamp: Long,
      difficulty: Int): Block = {
    var nonce = 0
    var nextHash = ""
    val merkleHash = MerkleNode.computeRoot(transactions)

    while (!isWithValidDifficulty(nextHash, difficulty)) {
      nonce += 1
      nextHash = hashOfHeaderFields(nextIndex, prevHash, newBlockData, merkleHash, timestamp, nonce)
    }

    Block(nextIndex, prevHash, newBlockData, merkleHash, transactions, timestamp, nonce)
  }

  def mineNextBlock(
      currentBlock: Block,
      newBlockData: String,
      transactions: List[Transaction],
      timestamp: Long,
      difficulty: Int): Block =
    mineNextBlock(currentBlock.index + 1, currentBlock.hash, newBlockData, transactions, timestamp, difficulty)


}

