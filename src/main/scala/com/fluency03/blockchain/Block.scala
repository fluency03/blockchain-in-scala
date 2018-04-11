package com.fluency03.blockchain

import java.time.Instant

import com.fluency03.blockchain.Util._
import com.fluency03.blockchain.BlockHeader._
import org.json4s.JObject
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._

case class BlockHeader(index: Long, previousHash: String, data: String, timestamp: Long, nonce: Int) {
  lazy val hash: String = hashOfBlockHeader(this)

  def isValidWith(difficulty: Int): Boolean = isWithValidDifficulty(hash, difficulty)

  def next(): BlockHeader = BlockHeader(index, previousHash, data, timestamp, nonce + 1)

  def toJson: JObject =
    ("index" -> index) ~
      ("previousHash" -> previousHash) ~
      ("data" -> data) ~
      ("timestamp" -> timestamp) ~
      ("hash" -> hash) ~
      ("nonce" -> nonce)

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
      ("transactions" -> transactions.map(_.toJson))

  override def toString: String = compact(render(toJson))

}


object Block {

  def apply(index: Long, previousHash: String, data: String, timestamp: Long, nonce: Int): Block =
    new Block(BlockHeader(index, previousHash, data, timestamp, nonce))

  // 00000988d7bdaf1b8f0b330836cb390e7002309f03ba61a080d846b083469f28, 1523472721000, 306408
  def genesis(): Block =
    Block(0, "0", "Welcome to Blockchain in Scala!", Instant.parse("2018-04-11T18:52:01Z").toEpochMilli, 306408)


}

