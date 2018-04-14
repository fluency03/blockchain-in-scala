package com.fluency03.blockchain.core

import com.fluency03.blockchain.core.BlockHeader.hashOfBlockHeader
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods.parse
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
        "nonce":13860,
        "hash":"00003607219f7a455e216f19ac3a34e3b158cf7282f7fdc624c93d593c2fc61f",
      },
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
          ("nonce" -> 13860) ~
          ("hash" -> "00003607219f7a455e216f19ac3a34e3b158cf7282f7fdc624c93d593c2fc61f")) ~
        ("transactions" -> List(genesisTx).map(_.toJson))
    genesis.toJson shouldEqual json
    parse(genesis.toString) shouldEqual json
  }

  "Next trial of Genesis block" should "equal to Genesis block except nonce+1 ." in {
    val genesisNextTrial: Block = genesis.nextTrial()
    genesisNextTrial.index shouldEqual genesis.index
    genesisNextTrial.previousHash shouldEqual genesis.previousHash
    genesisNextTrial.data shouldEqual genesis.data
    genesisNextTrial.merkleHash shouldEqual genesisTx.hash
    genesisNextTrial.timestamp shouldEqual genesis.timestamp
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
          ("nonce" -> (genesis.nonce + 1)) ~
          ("hash" -> newHash)) ~
        ("transactions" -> List(genesisTx).map(_.toJson))
    genesisNextTrial.toJson shouldEqual json
    parse(genesisNextTrial.toString) shouldEqual json
  }

  "Add Transactions to a Block" should "result to a new Block with new List of Transactions." in {
    val t1: Transaction = Transaction(ZERO64, ZERO64, 10)

    val newBlock: Block = genesis.addTransaction(ZERO64, ZERO64, 10)
    newBlock.index shouldEqual genesis.index
    newBlock.previousHash shouldEqual genesis.previousHash
    newBlock.data shouldEqual genesis.data
    val newMerkleHash = MerkleNode.computeRoot(t1 :: genesis.transactions)
    newBlock.merkleHash shouldEqual newMerkleHash
    newBlock.timestamp shouldEqual genesis.timestamp
    newBlock.nonce shouldEqual genesis.nonce
    val newHash = hashOfBlockHeader(newBlock.header)
    newBlock.hash shouldEqual newHash
    newBlock.hasValidMerkleHash shouldEqual true
    newBlock.hasValidHash(4) shouldEqual false
    newBlock.isValid(4) shouldEqual false
    val json =
      ("header" ->
        ("index" -> 0) ~
          ("previousHash" -> ZERO64) ~
          ("data" -> "Welcome to Blockchain in Scala!") ~
          ("merkleHash" -> newMerkleHash) ~
          ("timestamp" -> 1523472721) ~
          ("nonce" -> genesis.nonce) ~
          ("hash" -> newHash)) ~
        ("transactions" -> (t1 :: genesis.transactions).map(_.toJson))
    newBlock.toJson shouldEqual json
    parse(newBlock.toString) shouldEqual json
  }

  "Add a List of Transactions to a Block" should "result to a new Block with new List of Transactions." in {
    val t1: Transaction = Transaction(ZERO64, ZERO64, 10)
    val t2: Transaction = Transaction(ZERO64, ZERO64, 20)
    val t3: Transaction = Transaction(ZERO64, ZERO64, 30)
    val t4: Transaction = Transaction(ZERO64, ZERO64, 40)

    val newBlock: Block = genesis.addTransactions(t1 :: t2 :: t3 :: t4 :: Nil)
    newBlock.index shouldEqual genesis.index
    newBlock.previousHash shouldEqual genesis.previousHash
    newBlock.data shouldEqual genesis.data
    val newMerkleHash = MerkleNode.computeRoot(t1 :: t2 :: t3 :: t4 :: genesis.transactions)
    newBlock.merkleHash shouldEqual newMerkleHash
    newBlock.timestamp shouldEqual genesis.timestamp
    newBlock.nonce shouldEqual genesis.nonce
    val newHash = hashOfBlockHeader(newBlock.header)
    newBlock.hash shouldEqual newHash
    newBlock.hasValidMerkleHash shouldEqual true
    newBlock.hasValidHash(4) shouldEqual false
    newBlock.isValid(4) shouldEqual false
    val json =
      ("header" ->
        ("index" -> 0) ~
          ("previousHash" -> ZERO64) ~
          ("data" -> "Welcome to Blockchain in Scala!") ~
          ("merkleHash" -> newMerkleHash) ~
          ("timestamp" -> 1523472721) ~
          ("nonce" -> genesis.nonce) ~
          ("hash" -> newHash)) ~
        ("transactions" -> (t1 :: t2 :: t3 :: t4 :: genesis.transactions).map(_.toJson))
    newBlock.toJson shouldEqual json
    parse(newBlock.toString) shouldEqual json
  }





}
