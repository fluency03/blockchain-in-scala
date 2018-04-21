package com.fluency03.blockchain.core

import com.fluency03.blockchain.core.BlockHeader.hashOfBlockHeader
import com.fluency03.blockchain.core.Transaction.createCoinbaseTx
import org.json4s.JValue
import org.json4s.JsonAST.{JArray, JInt, JObject, JString}
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods.parse
import org.scalatest.{FlatSpec, Matchers}

import scala.io.Source

class BlockTest extends FlatSpec with Matchers {

  val genesis: Block = Block.genesisBlock
  val genesisTx: Transaction = createCoinbaseTx(0, genesisMiner, genesisTimestamp)

  val expectedBlockJson: JValue = parse(Source.fromResource("genesis-block.json").mkString)
  val expectedGenesisBlock: Block = expectedBlockJson.extract[Block]
  val expectedHeader: BlockHeader = expectedGenesisBlock.header

  "Genesis block" should "be a valid Genesis block." in {
    genesis.header shouldEqual expectedHeader
    genesis.index shouldEqual expectedHeader.index
    genesis.previousHash shouldEqual expectedHeader.previousHash
    genesis.data shouldEqual expectedHeader.data
    genesis.merkleHash shouldEqual expectedHeader.merkleHash
    genesis.merkleHash shouldEqual expectedGenesisBlock.transactions.head.id
    genesis.timestamp shouldEqual expectedHeader.timestamp
    genesis.nonce shouldEqual expectedHeader.nonce
    genesis.hash shouldEqual expectedHeader.hash
    genesis.isValid shouldEqual true
    genesis.toJson shouldEqual expectedBlockJson
    parse(genesis.toString) shouldEqual expectedBlockJson
  }

  "Next trial of Genesis block" should "equal to Genesis block except nonce+1 ." in {
    val genesisNextTrial: Block = genesis.nextTrial()
    val newExpectedHeader = expectedHeader.nextTrial()
    genesisNextTrial.header shouldEqual newExpectedHeader
    genesisNextTrial.index shouldEqual newExpectedHeader.index
    genesisNextTrial.previousHash shouldEqual newExpectedHeader.previousHash
    genesisNextTrial.data shouldEqual newExpectedHeader.data
    genesisNextTrial.merkleHash shouldEqual newExpectedHeader.merkleHash
    genesisNextTrial.merkleHash shouldEqual expectedGenesisBlock.transactions.head.id
    genesisNextTrial.timestamp shouldEqual newExpectedHeader.timestamp
    genesisNextTrial.nonce shouldEqual newExpectedHeader.nonce
    val newHash = hashOfBlockHeader(genesisNextTrial.header)
    genesisNextTrial.hash shouldEqual newHash
    genesisNextTrial.hash shouldEqual newExpectedHeader.hash
    genesisNextTrial.hasValidMerkleHash shouldEqual true
    genesisNextTrial.isValid shouldEqual false
    val headerJson = expectedHeader.toJson.transformField {
      case ("nonce", JInt(x)) => ("nonce", JInt(x+1))
    }
    val json = expectedBlockJson.transformField {
      case ("header", JObject(_)) => ("header", headerJson)
    }.transformField {
      case ("hash", JString(_)) => ("hash", newHash)
    }
    genesisNextTrial.toJson shouldEqual json
    parse(genesisNextTrial.toString) shouldEqual json
  }

  "Add Transactions to a Block" should "result to a new Block with new List of Transactions." in {
    val t1: Transaction = createCoinbaseTx(1, genesisMiner, genesisTimestamp)

    val newBlock: Block = genesis.addTransaction(t1)
    newBlock.index shouldEqual expectedGenesisBlock.index
    newBlock.previousHash shouldEqual expectedGenesisBlock.previousHash
    newBlock.data shouldEqual expectedGenesisBlock.data
    val newMerkleHash = Merkle.computeRoot(t1 +: expectedGenesisBlock.transactions)
    newBlock.merkleHash shouldEqual newMerkleHash
    newBlock.timestamp shouldEqual expectedGenesisBlock.timestamp
    newBlock.nonce shouldEqual expectedGenesisBlock.nonce
    val newHash = hashOfBlockHeader(newBlock.header)
    newBlock.hash shouldEqual newHash
    newBlock.hasValidMerkleHash shouldEqual true
    newBlock.isValid shouldEqual false
    val headerJson = expectedHeader.toJson.transformField {
      case ("merkleHash", JString(_)) => ("merkleHash", newMerkleHash)
    }
    val json = expectedBlockJson.transformField {
      case ("header", JObject(_)) => ("header", headerJson)
    }.transformField {
      case ("hash", JString(_)) => ("hash", newHash)
    }.transformField {
      case ("transactions", JArray(arr)) => ("transactions", JArray(t1.toJson +: arr))
    }
    newBlock.toJson shouldEqual json
    parse(newBlock.toString) shouldEqual json
  }

  "Add a List of Transactions to a Block" should "result to a new Block with new List of Transactions." in {
    val t1: Transaction = createCoinbaseTx(1, genesisMiner, genesisTimestamp)
    val t2: Transaction = createCoinbaseTx(2, genesisMiner, genesisTimestamp)
    val t3: Transaction = createCoinbaseTx(3, genesisMiner, genesisTimestamp)
    val t4: Transaction = createCoinbaseTx(4, genesisMiner, genesisTimestamp)

    val newBlock: Block = genesis.addTransactions(t1 :: t2 :: t3 :: t4 :: Nil)
    newBlock.index shouldEqual expectedGenesisBlock.index
    newBlock.previousHash shouldEqual expectedGenesisBlock.previousHash
    newBlock.data shouldEqual expectedGenesisBlock.data
    val newMerkleHash = Merkle.computeRoot(t1 +: t2 +: t3 +: t4 +: genesis.transactions)
    newBlock.merkleHash shouldEqual newMerkleHash
    newBlock.timestamp shouldEqual expectedGenesisBlock.timestamp
    newBlock.nonce shouldEqual expectedGenesisBlock.nonce
    val newHash = hashOfBlockHeader(newBlock.header)
    newBlock.hash shouldEqual newHash
    newBlock.hasValidMerkleHash shouldEqual true
    newBlock.isValid shouldEqual false
    val headerJson = expectedHeader.toJson.transformField {
      case ("merkleHash", JString(_)) => ("merkleHash", newMerkleHash)
    }
    val json = expectedBlockJson.transformField {
      case ("header", JObject(_)) => ("header", headerJson)
    }.transformField {
      case ("hash", JString(_)) => ("hash", newHash)
    }.transformField {
      case ("transactions", JArray(arr)) => ("transactions", JArray(t1.toJson :: t2.toJson :: t3.toJson :: t4.toJson +: arr))
    }
    newBlock.toJson shouldEqual json
    parse(newBlock.toString) shouldEqual json
  }



}
