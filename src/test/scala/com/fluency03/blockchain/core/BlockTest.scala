package com.fluency03.blockchain.core

import com.fluency03.blockchain.core.BlockHeader.hashOfBlockHeader
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods.parse
import org.scalatest.{FlatSpec, Matchers}

class BlockTest extends FlatSpec with Matchers {

  val genesis: Block = Block.genesisBlock
  val genesisTx: Transaction = Transaction(ZERO64, ZERO64, 50, genesisTimestamp)

  "Genesis block" should "be a valid Genesis block." in {
    genesis.index shouldEqual 0
    genesis.previousHash shouldEqual ZERO64
    genesis.data shouldEqual "Welcome to Blockchain in Scala!"
    genesis.merkleHash shouldEqual genesisTx.hash
    genesis.timestamp shouldEqual genesisTimestamp
    genesis.nonce shouldEqual 33660
    genesis.hash shouldEqual "0000a26af9a70022a6c6d270a0ced7478eb40bcfc4301b5e73c0ed3207a3de0e"
    genesis.hasValidMerkleHash shouldEqual true
    genesis.hasValidHash(4) shouldEqual true
    genesis.isValid(4) shouldEqual true
    val json =
      ("header" ->
          ("index" -> 0) ~
          ("previousHash" -> ZERO64) ~
          ("data" -> "Welcome to Blockchain in Scala!") ~
          ("merkleHash" -> genesisTx.hash) ~
          ("timestamp" -> genesisTimestamp) ~
          ("nonce" -> 33660) ~
          ("hash" -> "0000a26af9a70022a6c6d270a0ced7478eb40bcfc4301b5e73c0ed3207a3de0e")) ~
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
