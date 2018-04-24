package com.fluency03.blockchain
package core

import com.fluency03.blockchain.core.BlockHeader.hashOfBlockHeader
import com.fluency03.blockchain.core.Transaction.createCoinbaseTx
import org.json4s.JValue
import org.json4s.JsonAST.JInt
import org.json4s.native.JsonMethods.parse
import org.scalatest.{FlatSpec, Matchers}

import scala.io.Source

class BlockHeaderTest extends FlatSpec with Matchers  {

  val genesisHeader: BlockHeader = Block.genesisBlock.header
  val genesisTx: Transaction = createCoinbaseTx(0, genesisMiner, genesisTimestamp)

  val expectedBlockJson: JValue = parse(Source.fromResource("genesis-block.json").mkString)
  val expectedGenesisBlock: Block = expectedBlockJson.extract[Block]
  val expectedHeader: BlockHeader = expectedGenesisBlock.header

  "Genesis block header" should "be valid." in {
    genesisHeader shouldEqual expectedHeader
    genesisHeader.index shouldEqual expectedHeader.index
    genesisHeader.previousHash shouldEqual expectedHeader.previousHash
    genesisHeader.data shouldEqual expectedHeader.data
    genesisHeader.merkleHash shouldEqual expectedHeader.merkleHash
    genesisHeader.merkleHash shouldEqual genesisTx.id
    genesisHeader.timestamp shouldEqual expectedHeader.timestamp
    genesisHeader.nonce shouldEqual expectedHeader.nonce
    genesisHeader.hash shouldEqual expectedHeader.hash
    genesisHeader.toJson shouldEqual expectedBlockJson \ "header"
    parse(genesisHeader.toString) shouldEqual expectedBlockJson \ "header"
  }

  "Next trial of Genesis block header" should "equal to Genesis header except nonce+1 ." in {
    val genesisHeaderNextTrial: BlockHeader = genesisHeader.nextTrial()
    genesisHeaderNextTrial.index shouldEqual expectedHeader.index
    genesisHeaderNextTrial.previousHash shouldEqual expectedHeader.previousHash
    genesisHeaderNextTrial.data shouldEqual expectedHeader.data
    genesisHeaderNextTrial.merkleHash shouldEqual expectedHeader.merkleHash
    genesisHeaderNextTrial.merkleHash shouldEqual genesisTx.id
    genesisHeaderNextTrial.timestamp shouldEqual expectedHeader.timestamp
    genesisHeaderNextTrial.nonce shouldEqual expectedHeader.nonce + 1
    val newHash = hashOfBlockHeader(genesisHeaderNextTrial)
    genesisHeaderNextTrial.hash shouldEqual newHash
    val json = expectedHeader.toJson.transformField {
      case ("nonce", JInt(x)) => ("nonce", JInt(x+1))
    }
    genesisHeaderNextTrial.toJson shouldEqual json
    parse(genesisHeaderNextTrial.toString) shouldEqual json
  }


}
