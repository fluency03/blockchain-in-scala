package com.fluency03.blockchain.core

import com.fluency03.blockchain.Util.{hashOf, isWithValidDifficulty}
import com.fluency03.blockchain.core.BlockHeader.hashOfBlockHeader
import org.json4s.native.JsonMethods.{compact, render}
import org.json4s.{Extraction, JValue}

/**
 * Header of current Block.
 *
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
