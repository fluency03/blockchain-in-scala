package com.fluency03.blockchain

import java.time.Instant

import com.fluency03.blockchain.Util._
import com.fluency03.blockchain.BlockHeader._
import com.fluency03.blockchain.merkle.MerkleNode
import org.json4s.{Extraction, JValue, JObject, NoTypeHints}
import org.json4s.native.Serialization
import org.json4s.native.JsonMethods.{compact, render}
import org.json4s.JsonDSL._

case class BlockHeader(index: Long, previousHash: String, data: String, timestamp: Long, nonce: Int) {
  implicit val formats = Serialization.formats(NoTypeHints)
  lazy val hash: String = hashOfBlockHeader(this)

  def isValidWith(difficulty: Int): Boolean = isWithValidDifficulty(hash, difficulty)

  def next(): BlockHeader = BlockHeader(index, previousHash, data, timestamp, nonce + 1)

  def toJson: JValue = Extraction.decompose(this)

  override def toString: String = compact(render(toJson))

}

object BlockHeader {

  def hashOfBlockHeader(header: BlockHeader): String =
    hashOfHeaderFields(
      header.index,
      header.previousHash,
      header.data,
      header.timestamp,
      header.nonce
    )

  def hashOfHeaderFields(index: Long, previousHash: String, data: String, timestamp: Long, nonce: Int): String =
    hashOf(index.toString, previousHash, data, timestamp.toString, nonce.toString)

}


case class Block(header: BlockHeader, transactions: List[Transaction] = List()) {
  lazy val hash: String = header.hash
  def merkleRootHash: String = MerkleNode.computeRoot(transactions)

  def isValidWith(difficulty: Int): Boolean = header.isValidWith(difficulty)

  def next(): Block = Block(header.next(), transactions)

  def addTransaction(t: Transaction): Block =
    Block(header, t :: transactions)

  def addTransaction(sender: String, receiver: String, amount: Double): Block =
    addTransaction(Transaction(sender, receiver, amount))

  def addTransactions(trans: List[Transaction]): Block =
    Block(header, trans)

  def toJson: JObject =
    ("header" -> header.toJson) ~
      ("hash" -> hash) ~
      ("transactions" -> transactions.map(_.toJson))

  override def toString: String = compact(render(toJson))

}


object Block {

  def apply(index: Long, previousHash: String, data: String, timestamp: Long, nonce: Int): Block =
    new Block(BlockHeader(index, previousHash, data, timestamp, nonce))

  // 000031bee3fa033f2d69ae7d0d9f565bf3a235452ccf8a5edffb78cfbcdd7137, 1523472721, 187852
  lazy val genesisBlock: Block = genesis()

  def genesis(): Block =
    mineNextBlock(
      0,
      "0",
      "Welcome to Blockchain in Scala!",
      Instant.parse("2018-04-11T18:52:01Z").getEpochSecond,
      4
    )

  def mineNextBlock(nextIndex: Int, prevHash: String, newBlockData: String, timestamp: Long, difficulty: Int): Block = {
    var nonce = 0
    var nextHash = ""

    while (!isWithValidDifficulty(nextHash, difficulty)) {
      nonce += 1
      nextHash = hashOfHeaderFields(nextIndex, prevHash, newBlockData, timestamp, nonce)
    }

    Block(nextIndex, prevHash, newBlockData, timestamp, nonce)
  }

}

