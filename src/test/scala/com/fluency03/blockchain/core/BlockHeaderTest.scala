package com.fluency03.blockchain.core

import com.fluency03.blockchain.core.BlockHeader.hashOfBlockHeader
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods.parse
import org.scalatest.{FlatSpec, Matchers}

class BlockHeaderTest extends FlatSpec with Matchers  {

  val genesisHeader: BlockHeader = Block.genesisBlock.header
  val genesisTx: Transaction = Transaction(ZERO64, ZERO64, 50, genesisTimestamp)

  "Genesis block header" should "be valid." in {
    genesisHeader.index shouldEqual 0
    genesisHeader.previousHash shouldEqual ZERO64
    genesisHeader.data shouldEqual "Welcome to Blockchain in Scala!"
    genesisHeader.merkleHash shouldEqual genesisTx.hash
    genesisHeader.timestamp shouldEqual genesisTimestamp
    genesisHeader.nonce shouldEqual 33660
    genesisHeader.hash shouldEqual "0000a26af9a70022a6c6d270a0ced7478eb40bcfc4301b5e73c0ed3207a3de0e"
    genesisHeader.isValidWith(4) shouldEqual true
    val json = ("index" -> 0) ~
        ("previousHash" -> ZERO64) ~
        ("data" -> "Welcome to Blockchain in Scala!") ~
        ("merkleHash" -> genesisTx.hash) ~
        ("timestamp" -> genesisTimestamp) ~
        ("nonce" -> 33660) ~
        ("hash" -> "0000a26af9a70022a6c6d270a0ced7478eb40bcfc4301b5e73c0ed3207a3de0e")
    genesisHeader.toJson shouldEqual json
    parse(genesisHeader.toString) shouldEqual json
  }

  "Next trial of Genesis block header" should "equal to Genesis header except nonce+1 ." in {
    val genesisHeaderNextTrial: BlockHeader = genesisHeader.nextTrial()
    genesisHeaderNextTrial.index shouldEqual genesisHeader.index
    genesisHeaderNextTrial.previousHash shouldEqual genesisHeader.previousHash
    genesisHeaderNextTrial.data shouldEqual genesisHeader.data
    genesisHeaderNextTrial.merkleHash shouldEqual genesisTx.hash
    genesisHeaderNextTrial.timestamp shouldEqual genesisHeader.timestamp
    genesisHeaderNextTrial.nonce shouldEqual genesisHeader.nonce + 1
    val newHash = hashOfBlockHeader(genesisHeaderNextTrial)
    genesisHeaderNextTrial.hash shouldEqual newHash
    genesisHeaderNextTrial.isValidWith(4) shouldEqual false
    val json = ("index" -> 0) ~
        ("previousHash" -> ZERO64) ~
        ("data" -> "Welcome to Blockchain in Scala!") ~
        ("merkleHash" -> genesisTx.hash) ~
        ("timestamp" -> 1523472721) ~
        ("nonce" -> (genesisHeader.nonce + 1)) ~
        ("hash" -> newHash)
    genesisHeaderNextTrial.toJson shouldEqual json
    parse(genesisHeaderNextTrial.toString) shouldEqual json
  }


}
