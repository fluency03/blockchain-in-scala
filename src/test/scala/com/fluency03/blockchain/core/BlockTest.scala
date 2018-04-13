package com.fluency03.blockchain.core

import com.fluency03.blockchain.core.BlockHeader.hashOfBlockHeader
import org.json4s.JsonAST.JArray
import org.json4s.JsonDSL._
import org.scalatest.{FlatSpec, Matchers}

class BlockTest extends FlatSpec with Matchers {

  val genesis: Block = Block.genesisBlock
  // {"header":{"index":0,"previousHash":"0","data":"Welcome to Blockchain in Scala!","merkleHash":"7814a9c43e9015462e5ffec1a3a9a69be024c1aacfa3ec4c879b5cd544761e7e","timestamp":1523472721,"nonce":68955},"hash":"000009eeca07c148fdf4abaaabd73666df3d0b56b42b89a0c1c1db71d47333be","transactions":[]}
  println(genesis)

  "Genesis block" should "be a valid Genesis block." in {
    genesis.index shouldEqual 0
    genesis.previousHash shouldEqual ZERO64
    genesis.data shouldEqual "Welcome to Blockchain in Scala!"
    genesis.merkleHash shouldEqual "7814a9c43e9015462e5ffec1a3a9a69be024c1aacfa3ec4c879b5cd544761e7e"
    genesis.timestamp shouldEqual 1523472721
    genesis.nonce shouldEqual 187852
    genesis.hash shouldEqual "000031bee3fa033f2d69ae7d0d9f565bf3a235452ccf8a5edffb78cfbcdd7137"
    genesis.hasValidMerkleHash shouldEqual true
    genesis.hasValidHash(4) shouldEqual true
    genesis.isValid(4) shouldEqual true
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
      "{\"header\":{\"index\":0,\"previousHash\":\"0\",\"data\":\"Welcome to Blockchain in Scala!\"," +
        "\"merkleHash\":\"\",\"timestamp\":1523472721,\"nonce\":187852},\"hash\":" +
        "\"000031bee3fa033f2d69ae7d0d9f565bf3a235452ccf8a5edffb78cfbcdd7137\",\"transactions\":[]}"
  }

  "Next trial of Genesis block" should "equal to Genesis block except nonce+1 ." in {
    val genesisNextTrial: Block = genesis.nextTrial()
    genesisNextTrial.index shouldEqual 0
    genesisNextTrial.previousHash shouldEqual ZERO64
    genesisNextTrial.data shouldEqual "Welcome to Blockchain in Scala!"
    genesisNextTrial.merkleHash shouldEqual ""
    genesisNextTrial.timestamp shouldEqual 1523472721
    genesisNextTrial.nonce shouldEqual 187853
    val newHash = hashOfBlockHeader(genesisNextTrial.header)
    genesisNextTrial.hash shouldEqual newHash
    genesisNextTrial.hasValidMerkleHash shouldEqual true
    genesisNextTrial.hasValidHash(4) shouldEqual false
    genesisNextTrial.isValid(4) shouldEqual false
    val json =
      ("header" ->
          ("index" -> 0) ~
          ("previousHash" -> "0") ~
          ("data" -> "Welcome to Blockchain in Scala!") ~
          ("merkleHash" -> "") ~
          ("timestamp" -> 1523472721) ~
          ("nonce" -> 187853)) ~
        ("hash" -> newHash) ~
        ("transactions" -> JArray.apply(List()))
    genesisNextTrial.toJson shouldEqual json
    genesisNextTrial.toString shouldEqual
      "{\"header\":{\"index\":0,\"previousHash\":\"0\",\"data\":\"Welcome to Blockchain in Scala!\"," +
        "\"merkleHash\":\"\",\"timestamp\":1523472721,\"nonce\":187853},\"hash\":" +
        "\"" + newHash + "\",\"transactions\":[]}"
  }


}
