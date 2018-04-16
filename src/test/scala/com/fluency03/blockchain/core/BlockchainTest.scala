package com.fluency03.blockchain.core

import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class BlockchainTest extends FlatSpec with Matchers  {

  val blockchain: Blockchain = Blockchain()
  val genesis: Block = Block.genesisBlock

  val blockchainOf5: Blockchain = Blockchain(5)
  val genesisOf5: Block = Block.genesis(5)

  val t1: Transaction = Transaction(ZERO64, ZERO64, 10)
  val t2: Transaction = Transaction(ZERO64, ZERO64, 20)
  val t3: Transaction = Transaction(ZERO64, ZERO64, 30)
  val t4: Transaction = Transaction(ZERO64, ZERO64, 40)

  "A new Blockchain" should "have all default values." in {
    blockchain.difficulty shouldEqual 4
    blockchain.chain shouldEqual List(genesis)
    blockchain.lastBlock().isEmpty shouldEqual false
    blockchain.lastBlock().get shouldEqual genesis
    blockchain.currentTransactions shouldEqual mutable.Map.empty[String, Transaction]
    blockchain.isValid shouldEqual true
  }

  "A new Blockchain with different difficulty" should "have all default values but the difficulty." in {
    blockchainOf5.difficulty shouldEqual 5
    blockchainOf5.chain shouldEqual List(genesisOf5)
    blockchainOf5.lastBlock().isEmpty shouldEqual false
    blockchainOf5.lastBlock().get shouldEqual genesisOf5
    blockchainOf5.currentTransactions shouldEqual mutable.Map.empty[String, Transaction]
    blockchainOf5.isValid shouldEqual true
  }

  "Add a Transaction to a Blockchain" should "add these Transactions to its currentTransactions collection." in {
    val trans = mutable.Map.empty[String, Transaction]
    blockchain.addTransaction(t1)
    trans += (t1.hash -> t1)
    blockchain.currentTransactions shouldEqual trans

    blockchain.addTransaction(t2)
    blockchain.addTransaction(t3)
    trans += (t2.hash -> t2)
    trans += (t3.hash -> t3)
    blockchain.currentTransactions shouldEqual trans
    blockchain.isValid shouldEqual true
  }

  "Add a List of Transaction to a Blockchain" should "add these Transactions to its currentTransactions collection." in {
    val trans = mutable.Map.empty[String, Transaction]
    blockchainOf5.addTransactions(t2 :: t3 :: t4 :: Nil)
    trans += (t2.hash -> t2)
    trans += (t3.hash -> t3)
    trans += (t4.hash -> t4)
    blockchainOf5.currentTransactions shouldEqual trans
    blockchainOf5.isValid shouldEqual true
  }

  "Blockchain" should "be able to mine the next Block." in {
    val blockchainToAdd: Blockchain = Blockchain()
    val genesis: Block = Block.genesisBlock

    val actual = blockchainToAdd.mineNextBlock("This is next Block!")
    val expected = Block.mineNextBlock(genesis, "This is next Block!", List(), actual.timestamp, blockchain.difficulty)
    actual shouldEqual expected

    blockchainToAdd.lastBlock().get shouldEqual genesis
    blockchainToAdd.isValid shouldEqual true
    val blockchainAdded = blockchainToAdd.addBlock(actual)
    blockchainAdded.lastBlock().get shouldEqual expected
    blockchainAdded.isValid shouldEqual true
  }


}
