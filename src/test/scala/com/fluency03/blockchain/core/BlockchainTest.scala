package com.fluency03.blockchain
package core

import com.fluency03.blockchain.core.Blockchain.updateUTxOs
import com.fluency03.blockchain.core.Transaction.createCoinbaseTx
import org.json4s.JValue
import org.json4s.native.JsonMethods.parse
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable
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

  "updateUTxOs" should "update the UTXOs from a latest Seq of transactions." in {
    val tx: Transaction = Transaction(
      Seq(TxIn(Outpoint("def0", 0), "abc1"),
        TxIn(Outpoint("def0", 1), "abc1")),
      Seq(TxOut("abc4", 40)),
      genesisTimestamp
    )

    val unspentTxOuts1: mutable.Map[Outpoint, TxOut] = mutable.Map.empty[Outpoint, TxOut]
    unspentTxOuts1 += (Outpoint("def0", 0) -> TxOut("abc4", 20))
    unspentTxOuts1 += (Outpoint("def0", 1) -> TxOut("abc4", 20))

    val unspentTxOuts2: mutable.Map[Outpoint, TxOut] = mutable.Map.empty[Outpoint, TxOut]
    updateUTxOs(Seq(tx), unspentTxOuts1.toMap) should not equal unspentTxOuts2

    unspentTxOuts2 += (Outpoint(tx.id, 0) -> TxOut("abc4", 40))
    updateUTxOs(Seq(tx), unspentTxOuts1.toMap) shouldEqual unspentTxOuts2
  }







}
