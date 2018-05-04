package com.fluency03.blockchain.api.actors

import akka.actor.{ActorRef, ActorSelection, ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.fluency03.blockchain.api.actors.BlockPoolActor.CreateBlock
import com.fluency03.blockchain.api.actors.BlockchainActor._
import com.fluency03.blockchain.api.{BLOCKCHAIN_ACTOR_NAME, BLOCK_POOL_ACTOR_NAME, FailureMsg, SuccessMsg}
import com.fluency03.blockchain.core.{Block, Blockchain, Transaction}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

class BlockchainActorTest extends TestKit(ActorSystem("BlockchainActorTest")) with DefaultTimeout with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll: Unit = {
    shutdown()
  }

  val blockchainActor: ActorRef = system.actorOf(Props[BlockchainActor], BLOCKCHAIN_ACTOR_NAME)
  val blockPoolActor: ActorRef = system.actorOf(Props[BlockPoolActor], BLOCK_POOL_ACTOR_NAME)

  "A BlockchainActor" should {
    "Respond with a Blockchain." in {
      BlockchainActor.props shouldEqual Props[BlockchainActor]

      blockchainActor ! GetBlockchain
      expectMsg(None)

      within(15 seconds) {
        blockchainActor ! CreateBlockchain
        expectMsgType[SuccessMsg]
      }

      blockchainActor ! CreateBlockchain
      expectMsgType[FailureMsg]

      blockchainActor ! GetBlockchain
      val blockchain = expectMsgType[Some[Blockchain]].get

      blockchainActor ! GetBlockFromChain("somehash")
      expectMsg(None)

      val genesis = Block.genesisBlock
      blockchainActor ! GetBlockFromChain(genesis.hash)
      expectMsg(Some(genesis))

      blockchainActor ! GetTxOfBlock(genesis.transactions.head.id, genesis.hash)
      expectMsg(Some(genesis.transactions.head))

      blockchainActor ! GetTxOfBlock("aa", genesis.hash)
      expectMsg(None)

      blockchainActor ! GetTxOfBlock(genesis.transactions.head.id, "bb")
      expectMsg(None)

      blockchainActor ! CheckBlockchainValidity
      expectMsg(SuccessMsg("true"))

      blockchainActor ! AddBlock(genesis)
      expectMsg(SuccessMsg(s"New Block ${genesis.hash} added on the chain."))

      blockchainActor ! CheckBlockchainValidity
      expectMsg(SuccessMsg("false"))

      blockchainActor ! RemoveBlock
      expectMsg(SuccessMsg(s"Last Block ${genesis.hash} removed from the chain."))

      blockchainActor ! CheckBlockchainValidity
      expectMsg(SuccessMsg("true"))

      blockPoolActor ! CreateBlock(genesis)
      expectMsg(SuccessMsg(s"Block ${genesis.hash} created in the Pool."))

      within(15 seconds) {
        blockchainActor ! GetBlockFromPool(genesis.hash)
        expectMsg(Some(genesis))

        blockchainActor ! GetBlockFromPool("someid")
        expectMsg(None)
      }

      within(30 seconds) {
        blockchainActor ! AddBlockFromPool(genesis.hash)
        expectMsg(SuccessMsg(s"New Block ${genesis.hash} added on the chain."))

        blockchainActor ! AddBlockFromPool("someid")
        expectMsg(FailureMsg(s"Did not find Block someid in the poll."))
      }

      blockchainActor ! RemoveBlock
      expectMsg(SuccessMsg(s"Last Block ${genesis.hash} removed from the chain."))

      within(15 seconds) {
        blockchainActor ! MineNextBlock("next", Seq.empty[String])
        val actualBlock = expectMsgType[Some[Block]].get
        actualBlock.data shouldEqual "next"
        actualBlock.transactions shouldEqual Seq.empty[Transaction]
        actualBlock.difficulty shouldEqual blockchain.difficulty
        actualBlock.index shouldEqual 1
        actualBlock.hasValidHash shouldEqual true
        actualBlock.previousHash shouldEqual genesis.hash
      }

      blockchainActor ! DeleteBlockchain
      expectMsg(SuccessMsg("Blockchain deleted."))

      blockchainActor ! DeleteBlockchain
      expectMsg(FailureMsg("Blockchain does not exist."))

      blockchainActor ! AddBlock(genesis)
      expectMsg(FailureMsg("Blockchain does not exist."))

      blockchainActor ! AddBlockFromPool(genesis.hash)
      expectMsg(FailureMsg("Blockchain does not exist."))

      blockchainActor ! RemoveBlock
      expectMsg(FailureMsg("Blockchain does not exist."))

      blockchainActor ! CheckBlockchainValidity
      expectMsg(FailureMsg("Blockchain does not exist."))

      blockchainActor ! MineNextBlock("next", Seq.empty[String])
      expectMsg(None)

      blockchainActor ! "other"
      expectNoMessage
    }
  }

}
