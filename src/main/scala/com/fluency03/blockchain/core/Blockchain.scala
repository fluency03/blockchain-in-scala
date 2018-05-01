package com.fluency03.blockchain
package core

import com.fluency03.blockchain.core.Block.validLinkBetween
import com.fluency03.blockchain.core.Blockchain._
import org.json4s.native.JsonMethods.{compact, render}
import org.json4s.{Extraction, JValue}

import scala.annotation.tailrec

/**
 * Blockchain with difficulty and the chain of Blocks.
 * @param difficulty Difficulty of a Blockchain
 * @param chain Chain of Blocks, the newest Block is on the head of the chain;
 *              the first genesis Block is on the last of the chain
 */
case class Blockchain(difficulty: Int = 4, chain: Seq[Block] = Seq(Block.genesisBlock)) {

  def addBlock(newBlockData: String, transactions: Seq[Transaction]): Blockchain =
    Blockchain(difficulty, mineNextBlock(newBlockData, transactions) +: chain)

  def addBlock(newBlock: Block): Blockchain = Blockchain(difficulty, newBlock +: chain)

  def removeBlock(): Blockchain = Blockchain(difficulty, chain.tail)

  def lastBlock(): Option[Block] = chain.headOption

  def mineNextBlock(newBlockData: String, transactions: Seq[Transaction]): Block = {
    val lastBlockOpt: Option[Block] = this.lastBlock()
    if (lastBlockOpt.isEmpty) throw new NoSuchElementException("Last Block does not exist!")
    val lastHeader = lastBlockOpt.get.header
    Block.mineNextBlock(lastHeader.index + 1, lastHeader.hash, newBlockData, getCurrentTimestamp, difficulty,
      transactions)
  }

  def isValid: Boolean = chain match {
    case Nil => throw new NoSuchElementException("Blockchain is Empty!")
    case _ => isValidChain(chain)
  }

  def length: Int = chain.length

  def toJson: JValue = Extraction.decompose(this)

  override def toString: String = compact(render(toJson))

}

object Blockchain {

  def apply(difficulty: Int): Blockchain = new Blockchain(difficulty, Seq(Block.genesis(difficulty)))

  // TODO (Chang): return both the validity and highest valid Block index
  @tailrec
  def isValidChain(chain: Seq[Block]): Boolean = chain match {
    case Nil => true
    case g +: Nil => g.previousHash == ZERO64 && g.index == 0 && g.hasValidHash
    case a +: b +: tail => validLinkBetween(a, b) && isValidChain(b +: tail)
  }

}

