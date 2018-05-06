package com.github.fluency03.blockchain
package core

import java.security.KeyPair

import com.github.fluency03.blockchain.core.Block.allTransValidOf
import com.github.fluency03.blockchain.core.BlockHeader.hashOfBlockHeader
import com.github.fluency03.blockchain.core.Transaction.{COINBASE_AMOUNT, createCoinbaseTx, signTxIn, updateUTxOs}
import com.github.fluency03.blockchain.crypto.Secp256k1
import org.json4s.JValue
import org.json4s.JsonAST.{JArray, JInt, JObject, JString}
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods.parse
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable
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
    genesis.hasValidHash shouldEqual true
    genesis.noDuplicateTxIn shouldEqual true
    genesis.toJson shouldEqual expectedBlockJson
    parse(genesis.toString) shouldEqual expectedBlockJson

    genesis shouldEqual Block(
      expectedHeader.index,
      expectedHeader.previousHash,
      expectedHeader.data,
      expectedHeader.timestamp,
      expectedHeader.difficulty,
      expectedHeader.nonce,
      expectedGenesisBlock.transactions)

    genesis.removeTransaction(genesisTx) shouldEqual Block(
      expectedHeader.index,
      expectedHeader.previousHash,
      expectedHeader.data,
      ZERO64,
      expectedHeader.timestamp,
      expectedHeader.difficulty,
      expectedHeader.nonce)
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
    genesisNextTrial.hasValidHash shouldEqual false
    genesisNextTrial.noDuplicateTxIn shouldEqual true
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
    newBlock.hasValidHash shouldEqual false
    newBlock.noDuplicateTxIn shouldEqual true
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
    newBlock.hasValidHash shouldEqual false
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

  "allTransAreValid" should "check whether all transactions of a Block are valid." in {
    val uTxOs: mutable.Map[Outpoint, TxOut] = mutable.Map.empty[Outpoint, TxOut]
    genesis.allTransAreValid(uTxOs) shouldEqual true
    allTransValidOf(Nil, 0, uTxOs) shouldEqual false

    val pair1 = Secp256k1.generateKeyPair()
    val address1 = pair1.getPublic.toHex

    val ts = getCurrentTimestamp
    val tx: Transaction = Transaction(
      Seq(TxIn(Outpoint(genesis.transactions.head.id, 0), "")),
      Seq(TxOut(address1, COINBASE_AMOUNT)),
      ts)

    val nextBlock = Block.mineNextBlock(genesis, "This is next Block!", ts, genesis.difficulty, Seq(tx))
    nextBlock.allTransAreValid(uTxOs) shouldEqual false

    val uTxOs2 = updateUTxOs(genesis.transactions, uTxOs.toMap)
    uTxOs ++= uTxOs2
    nextBlock.allTransAreValid(uTxOs) shouldEqual false

    val genesisPrivate: String = Source.fromResource("private-key").getLines.mkString
    val keyPair = new KeyPair(Secp256k1.recoverPublicKey(genesisMiner), Secp256k1.recoverPrivateKey(genesisPrivate))
    val signedTxIns = tx.txIns.map(txIn => signTxIn(tx.id.hex2Bytes, txIn, keyPair, uTxOs)).filter(_.isDefined).map(_.get)
    signedTxIns.length shouldEqual tx.txIns.length
    val signedTx = Transaction(signedTxIns, Seq(TxOut(address1, COINBASE_AMOUNT)), ts)

    val validNewBlock = Block.mineNextBlock(genesis, "This is next Block!", ts, genesis.difficulty,
      Seq(signedTx), address1)
    validNewBlock.allTransAreValid(uTxOs) shouldEqual true
  }

}
