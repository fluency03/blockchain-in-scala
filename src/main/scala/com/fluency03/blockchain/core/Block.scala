package com.fluency03.blockchain
package core

import com.fluency03.blockchain.core.Block.allTransValidOf
import com.fluency03.blockchain.core.BlockHeader.hashOfHeaderFields
import com.fluency03.blockchain.core.Transaction.{createCoinbaseTx, validateCoinbaseTx, noDuplicateTxInOf}
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

  def allTransAreValid(uTxOs: mutable.Map[Outpoint, TxOut]): Boolean = allTransValidOf(transactions, index, uTxOs)

  def noDuplicateTxIn(): Boolean = noDuplicateTxInOf(transactions)

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

  /**
   * Check whether transactions of a Block are valid:
   * 1. Coinbase transaction is valid
   * 2. Rest of the transactions are valid
   * 3. if the Seq is empty, then it is not valid, because it has to at least contain one coinbase transaction
   */
  def allTransValidOf(
      transactions: Seq[Transaction],
      blockIndex: Int,
      uTxOs: mutable.Map[Outpoint, TxOut])
    : Boolean = transactions match {
    case Nil => false
    case init :+ last => validateCoinbaseTx(last, blockIndex) && init.forall(tx => tx.isValid(uTxOs))
  }

  /**
   * Check whether a new Block can be chained to the last Block of a Blockchain (previousBlock of newBlock):
   * 1. New Block's index should be the previous index plus one
   * 2. New Block's previousHash should be the hash of previous Block
   * 3. New Block should have valid hash (that means, valid header hash and merkle hash)
   */
  def validLinkBetween(newBlock: Block, previousBlock: Block): Boolean =
    previousBlock.index + 1 == newBlock.index &&
      previousBlock.hash == newBlock.previousHash &&
      newBlock.hasValidHash


}

