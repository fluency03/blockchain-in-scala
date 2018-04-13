package com.fluency03.blockchain.core

import com.fluency03.blockchain.core.BlockHeader.hashOfBlockHeader
import org.json4s.JsonDSL._
import org.scalatest.{FlatSpec, Matchers}

class BlockTest extends FlatSpec with Matchers {

  /*
    {"header":
      {
        "index":0,
        "previousHash":"0000000000000000000000000000000000000000000000000000000000000000",
        "data":"Welcome to Blockchain in Scala!",
        "merkleHash":"7814a9c43e9015462e5ffec1a3a9a69be024c1aacfa3ec4c879b5cd544761e7e",
        "timestamp":1523472721,
        "nonce":13860
      },
      "hash":"00003607219f7a455e216f19ac3a34e3b158cf7282f7fdc624c93d593c2fc61f",
      "transactions":[
        {
          "sender":"0000000000000000000000000000000000000000000000000000000000000000",
          "receiver":"0000000000000000000000000000000000000000000000000000000000000000",
          "amount":50.0
        }
      ]
    }
  */
  val genesis: Block = Block.genesisBlock
  val genesisTx: Transaction = Transaction(ZERO64, ZERO64, 50)

  "Genesis block" should "be a valid Genesis block." in {
    genesis.index shouldEqual 0
    genesis.previousHash shouldEqual ZERO64
    genesis.data shouldEqual "Welcome to Blockchain in Scala!"
    genesis.merkleHash shouldEqual genesisTx.hash
    genesis.timestamp shouldEqual 1523472721
    genesis.nonce shouldEqual 13860
    genesis.hash shouldEqual "00003607219f7a455e216f19ac3a34e3b158cf7282f7fdc624c93d593c2fc61f"
    genesis.hasValidMerkleHash shouldEqual true
    genesis.hasValidHash(4) shouldEqual true
    genesis.isValid(4) shouldEqual true
    val json =
      ("header" ->
          ("index" -> 0) ~
          ("previousHash" -> ZERO64) ~
          ("data" -> "Welcome to Blockchain in Scala!") ~
          ("merkleHash" -> genesisTx.hash) ~
          ("timestamp" -> 1523472721) ~
          ("nonce" -> 13860)) ~
        ("hash" -> "00003607219f7a455e216f19ac3a34e3b158cf7282f7fdc624c93d593c2fc61f") ~
        ("transactions" -> List(genesisTx).map(_.toJson))
    genesis.toJson shouldEqual json
    genesis.toString shouldEqual
        "{\"header\":{\"index\":0,\"previousHash\":\"" + ZERO64 + "\",\"data\":\"Welcome to Blockchain in Scala!\"," +
          "\"merkleHash\":\"" + genesisTx.hash + "\",\"timestamp\":1523472721,\"nonce\":13860}," +
          "\"hash\":\"00003607219f7a455e216f19ac3a34e3b158cf7282f7fdc624c93d593c2fc61f\"," +
          "\"transactions\":[{\"sender\":\"" + ZERO64 + "\",\"receiver\":\"" + ZERO64 + "\",\"amount\":50.0}]}"
  }

  "Next trial of Genesis block" should "equal to Genesis block except nonce+1 ." in {
    val genesisNextTrial: Block = genesis.nextTrial()
    genesisNextTrial.index shouldEqual 0
    genesisNextTrial.previousHash shouldEqual ZERO64
    genesisNextTrial.data shouldEqual "Welcome to Blockchain in Scala!"
    genesisNextTrial.merkleHash shouldEqual genesisTx.hash
    genesisNextTrial.timestamp shouldEqual 1523472721
    genesisNextTrial.nonce shouldEqual genesis.nonce + 1
    val newHash = hashOfBlockHeader(genesisNextTrial.header)
    genesisNextTrial.hash shouldEqual newHash
    genesisNextTrial.hasValidMerkleHash shouldEqual true
    genesisNextTrial.hasValidHash(4) shouldEqual false
    genesisNextTrial.isValid(4) shouldEqual false
    val json =
      ("header" ->
          ("index" -> 0) ~
          ("previousHash" -> ZERO64) ~
          ("data" -> "Welcome to Blockchain in Scala!") ~
          ("merkleHash" -> genesisTx.hash) ~
          ("timestamp" -> 1523472721) ~
          ("nonce" -> (genesis.nonce + 1))) ~
        ("hash" -> newHash) ~
        ("transactions" -> List(genesisTx).map(_.toJson))
    genesisNextTrial.toJson shouldEqual json
    genesisNextTrial.toString shouldEqual
      "{\"header\":{\"index\":0,\"previousHash\":\"" + ZERO64 + "\",\"data\":\"Welcome to Blockchain in Scala!\"," +
        "\"merkleHash\":\"" + genesisTx.hash + "\",\"timestamp\":1523472721,\"nonce\":13861}," +
        "\"hash\":\"" + newHash + "\"," +
        "\"transactions\":[{\"sender\":\"" + ZERO64 + "\",\"receiver\":\"" + ZERO64 + "\",\"amount\":50.0}]}"
  }


}
