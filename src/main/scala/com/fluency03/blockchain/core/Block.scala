package com.fluency03.blockchain
package core

import com.fluency03.blockchain.core.BlockHeader.hashOfHeaderFields
import com.fluency03.blockchain.core.Transaction.createCoinbaseTx
import org.json4s.native.JsonMethods.{compact, render}
import org.json4s.{Extraction, JValue}

import scala.collection.mutable

/**
 * Block on the chain.
 * @param header Header of current Block
 * @param transactions Seq of Transactions included in current Block
 */
case class Block(header: BlockHeader, transactions: Seq[Transaction], hash: String) {
  def index: Int = header.index
  def previousHash: String = header.previousHash
  def data: String = header.data
  def merkleHash: String = header.merkleHash
  def timestamp: Long = header.timestamp
  def difficulty: Int = header.difficulty
  def nonce: Int = header.nonce

  def nextTrial(): Block = Block(header.nextTrial(), transactions)

  def addTransaction(tx: Transaction): Block =
    Block(index, previousHash, data, timestamp, difficulty, nonce, tx +: transactions)

  def addTransactions(trans: Seq[Transaction]): Block =
    Block(index, previousHash, data, timestamp, difficulty, nonce, trans ++ transactions)

  def removeTransaction(tx: Transaction): Block =
    Block(index, previousHash, data, timestamp, difficulty, nonce, transactions.filter(_ != tx))

  def hasValidHash: Boolean = hasValidHeaderHash && isWithValidDifficulty(hash, difficulty) && hasValidMerkleHash

  def hasValidMerkleHash: Boolean = merkleHash == Merkle.computeRoot(transactions)

  def hasValidHeaderHash: Boolean = hash == header.hash

  def allTransactionsValid(uTxOs: mutable.Map[Outpoint, TxOut]): Boolean = transactions.forall(tx => tx.isValid(uTxOs))

  def toJson: JValue = Extraction.decompose(this)

  override def toString: String = compact(render(toJson))
}

object Block {

  def apply(header: BlockHeader, transactions: Seq[Transaction] = Seq()): Block =
    Block(header, transactions, header.hash)

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

  def genesis(difficulty: Int = 4): Block = mineNextBlock(
    0, ZERO64, "Welcome to Blockchain in Scala!", genesisTimestamp, difficulty,
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

  // TODO (Chang): Check transactions in the new block?
  def canBeChained(newBlock: Block, previousBlock: Block): Boolean =
    previousBlock.index + 1 == newBlock.index && previousBlock.hash == newBlock.previousHash && newBlock.hasValidHash



}

