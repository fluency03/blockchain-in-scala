package com.fluency03.blockchain.core

import java.time.Instant

import com.fluency03.blockchain.Util.{isWithValidDifficulty, hashOf}
import com.fluency03.blockchain.core.BlockHeader.{hashOfBlockHeader, hashOfHeaderFields}
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods.{compact, render}
import org.json4s.native.Serialization
import org.json4s.{Extraction, JValue, NoTypeHints}

/**
 * Header of current Block.
 * @param index Index of current Block
 * @param previousHash Hash of previous Block
 * @param data Data attached to current Block
 * @param merkleHash Merkle root hash of current Block
 * @param timestamp Timestamp of current Block
 * @param nonce Nonce of current Block
 */
case class BlockHeader(
    index: Int,
    previousHash: String,
    data: String,
    merkleHash: String,
    timestamp: Long,
    nonce: Int) {

  implicit val formats = Serialization.formats(NoTypeHints)
  lazy val hash: String = hashOfBlockHeader(this)

  def isValidWith(difficulty: Int): Boolean = isWithValidDifficulty(hash, difficulty)

  def nextTrial(): BlockHeader = BlockHeader(index, previousHash, data, merkleHash, timestamp, nonce + 1)

  def toJson: JValue = Extraction.decompose(this)

  override def toString: String = compact(render(toJson))

}

object BlockHeader {

  def hashOfBlockHeader(header: BlockHeader): String =
    hashOfHeaderFields(
        header.index,
        header.previousHash,
        header.data,
        header.merkleHash,
        header.timestamp,
        header.nonce)

  def hashOfHeaderFields(
      index: Long,
      previousHash: String,
      data: String,
      merkleHash: String,
      timestamp: Long,
      nonce: Int): String =
    hashOf(index.toString, previousHash, data, merkleHash, timestamp.toString, nonce.toString)

}


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

  def next(): Block = Block(header.nextTrial(), transactions)

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
    new Block(BlockHeader(index, previousHash, data, merkleHash, timestamp, nonce))

  // 000031bee3fa033f2d69ae7d0d9f565bf3a235452ccf8a5edffb78cfbcdd7137, 1523472721, 187852
  lazy val genesisBlock: Block = genesis()

  def genesis(): Block =
    mineNextBlock(
        0,
        "0",
        "Welcome to Blockchain in Scala!",
        "",
        Instant.parse("2018-04-11T18:52:01Z").getEpochSecond,
        4)

  def mineNextBlock(
      nextIndex: Int,
      prevHash: String,
      newBlockData: String,
      merkleHash: String,
      timestamp: Long,
      difficulty: Int): Block = {
    var nonce = 0
    var nextHash = ""

    while (!isWithValidDifficulty(nextHash, difficulty)) {
      nonce += 1
      nextHash = hashOfHeaderFields(nextIndex, prevHash, newBlockData, merkleHash, timestamp, nonce)
    }

    Block(nextIndex, prevHash, newBlockData, merkleHash, timestamp, nonce)
  }

  def mineNextBlock(
      currentBlock: Block,
      newBlockData: String,
      merkleHash: String,
      timestamp: Long,
      difficulty: Int): Block =
    mineNextBlock(currentBlock.index + 1, currentBlock.hash, newBlockData, merkleHash, timestamp, difficulty)


}

