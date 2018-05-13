package com.github.fluency03.blockchain
package core

import com.github.fluency03.blockchain.core.BlockHeader.hashOfBlockHeader
import com.github.fluency03.blockchain.crypto.SHA256
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
 * @param difficulty Difficulty for current Block
 * @param nonce Nonce of current Block
 */
case class BlockHeader(
    index: Int,
    previousHash: String,
    data: String,
    merkleHash: String,
    timestamp: Long,
    difficulty: Int,
    nonce: Int) {

  lazy val hash: String = hashOfBlockHeader(this)

  def nextTrial(): BlockHeader =
    BlockHeader(index, previousHash, data, merkleHash, timestamp, difficulty, nonce + 1)

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
      header.difficulty,
      header.nonce)

  def hashOfHeaderFields(
      index: Int,
      previousHash: String,
      data: String,
      merkleHash: String,
      timestamp: Long,
      difficulty: Int,
      nonce: Int): String =
    SHA256.hashStrings(
      index.toString,
      previousHash,
      data,
      merkleHash,
      timestamp.toString,
      difficulty.toString,
      nonce.toString)

}
