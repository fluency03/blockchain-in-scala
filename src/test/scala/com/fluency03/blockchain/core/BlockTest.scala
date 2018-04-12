package com.fluency03.blockchain.core

import com.fluency03.blockchain.core.BlockHeader.hashOfBlockHeader
import org.json4s.JsonAST.JArray
import org.json4s.JsonDSL._
import org.scalatest.{FlatSpec, Matchers}

class BlockTest extends FlatSpec with Matchers {

  val genesis: Block = Block.genesisBlock

  "Genesis block" should "be a valid Genesis block." in {
    genesis.index shouldEqual 0
    genesis.previousHash shouldEqual "0"
    genesis.data shouldEqual "Welcome to Blockchain in Scala!"
    genesis.merkleHash shouldEqual ""
    genesis.timestamp shouldEqual 1523472721
    genesis.nonce shouldEqual 187852
    genesis.hash shouldEqual "000031bee3fa033f2d69ae7d0d9f565bf3a235452ccf8a5edffb78cfbcdd7137"
    genesis.hasValidHash(4) shouldEqual true
    val json =
      ("header" ->
          ("index" -> 0) ~
          ("previousHash" -> "0") ~
          ("data" -> "Welcome to Blockchain in Scala!") ~
          ("merkleHash" -> "") ~
          ("timestamp" -> 1523472721) ~
          ("nonce" -> 187852)) ~
        ("hash" -> "000031bee3fa033f2d69ae7d0d9f565bf3a235452ccf8a5edffb78cfbcdd7137") ~
        ("transactions" -> JArray.apply(List()))
    genesis.toJson shouldEqual json
    genesis.toString shouldEqual
      "{\"header\":{\"index\":0,\"previousHash\":\"0\",\"data\":\"Welcome to Blockchain in Scala!\",\"merkleHash\":\"\",\"timestamp\":1523472721,\"nonce\":187852},\"hash\":\"000031bee3fa033f2d69ae7d0d9f565bf3a235452ccf8a5edffb78cfbcdd7137\",\"transactions\":[]}"
  }

//  "Next trial of Genesis block header" should "equal to Genesis header except nonce+1 ." in {
//    val genesisHeaderNextTrial: BlockHeader = genesisHeader.nextTrial()
//    genesisHeaderNextTrial.index shouldEqual 0
//    genesisHeaderNextTrial.previousHash shouldEqual "0"
//    genesisHeaderNextTrial.data shouldEqual "Welcome to Blockchain in Scala!"
//    genesisHeaderNextTrial.merkleHash shouldEqual ""
//    genesisHeaderNextTrial.timestamp shouldEqual 1523472721
//    genesisHeaderNextTrial.nonce shouldEqual 187853
//    val newHash = hashOfBlockHeader(genesisHeaderNextTrial)
//    genesisHeaderNextTrial.hash shouldEqual newHash
//    genesisHeaderNextTrial.isValidWith(4) shouldEqual false
//    val json = ("index" -> 0) ~
//      ("previousHash" -> "0") ~
//      ("data" -> "Welcome to Blockchain in Scala!") ~
//      ("merkleHash" -> "") ~
//      ("timestamp" -> 1523472721) ~
//      ("nonce" -> 187853)
//    genesisHeaderNextTrial.toJson shouldEqual json
//    genesisHeaderNextTrial.toString shouldEqual
//      "{\"index\":0,\"previousHash\":\"0\",\"data\":\"Welcome to Blockchain in Scala!\",\"merkleHash\":\"\",\"timestamp\":1523472721,\"nonce\":187853}"
//  }


}
