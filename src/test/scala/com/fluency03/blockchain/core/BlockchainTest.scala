package com.fluency03.blockchain
package core

import com.fluency03.blockchain.core.Transaction.createCoinbaseTx
import org.json4s.JsonDSL._
import org.json4s.JValue
import org.json4s.JsonAST.JArray
import org.json4s.native.JsonMethods.{compact, parse, render}
import org.scalatest.{FlatSpec, Matchers}

import scala.io.Source

class BlockchainTest extends FlatSpec with Matchers  {

  val blockchain: Blockchain = Blockchain()
  val genesis: Block = Block.genesisBlock

  val expectedBlockJson: JValue = parse(Source.fromResource("genesis-block.json").mkString)
  val expectedGenesisBlock: Block = expectedBlockJson.extract[Block]
  val expectedHeader: BlockHeader = expectedGenesisBlock.header

  val blockchainOf5: Blockchain = Blockchain(5)
  val genesisOf5: Block = Block.genesis(5)

  val t1: Transaction = createCoinbaseTx(1, genesisMiner, genesisTimestamp)
  val t2: Transaction = createCoinbaseTx(2, genesisMiner, genesisTimestamp)
  val t3: Transaction = createCoinbaseTx(3, genesisMiner, genesisTimestamp)
  val t4: Transaction = createCoinbaseTx(4, genesisMiner, genesisTimestamp)

  "A new Blockchain" should "have all default values." in {
    blockchain.difficulty shouldEqual 4
    blockchain.chain shouldEqual List(expectedGenesisBlock)
    blockchain.lastBlock().isEmpty shouldEqual false
    blockchain.lastBlock().get shouldEqual expectedGenesisBlock
    blockchain.isValid shouldEqual true
    val json = ("difficulty" -> blockchain.difficulty) ~ ("chain" -> JArray(List(expectedBlockJson)))
    blockchain.toJson shouldEqual json
    parse(blockchain.toString) shouldEqual json

    a[NoSuchElementException] should be thrownBy Blockchain(4, Seq.empty[Block]).isValid
  }

  "A new Blockchain with different difficulty" should "have all default values but the difficulty." in {
    blockchainOf5.difficulty shouldEqual 5
    blockchainOf5.chain shouldEqual List(genesisOf5)
    blockchainOf5.lastBlock().isEmpty shouldEqual false
    blockchainOf5.lastBlock().get shouldEqual genesisOf5
    blockchainOf5.isValid shouldEqual true
  }

  "Blockchain" should "be able to mine the next Block." in {
    val blockchainToAdd: Blockchain = Blockchain()
    val genesis: Block = Block.genesisBlock

    val actual = blockchainToAdd.mineNextBlock("This is next Block!", Seq(t1, t2))
    val expected = Block.mineNextBlock(genesis, "This is next Block!", actual.timestamp, blockchain.difficulty, Seq(t1, t2))
    actual shouldEqual expected

    blockchainToAdd.lastBlock().get shouldEqual genesis
    blockchainToAdd.isValid shouldEqual true
    val blockchainAdded = blockchainToAdd.addBlock(actual)
    blockchainAdded.lastBlock().get shouldEqual expected
    blockchainAdded.isValid shouldEqual true
  }

  "Blockchain" should "have be validatable." in {
    // TODO (Chang): isValidChain
  }

}
