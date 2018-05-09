package com.github.fluency03.blockchain.api.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.github.fluency03.blockchain.api._
import com.github.fluency03.blockchain.api.actors.BlockPoolActor._
import com.github.fluency03.blockchain.api.actors.BlockchainActor.{CreateBlockchain, DeleteBlockchain}
import com.github.fluency03.blockchain.core.{Block, Transaction}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

class BlockPoolActorTest extends TestKit(ActorSystem("BlocksActorTest")) with DefaultTimeout with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll: Unit = {
    shutdown()
  }

  val blockchainActor: ActorRef = system.actorOf(Props[BlockchainActor], BLOCKCHAIN_ACTOR_NAME)
  val blockPoolActor: ActorRef = system.actorOf(Props[BlockPoolActor], BLOCK_POOL_ACTOR_NAME)
  val txPoolActor: ActorRef = system.actorOf(Props[TxPoolActor], TX_POOL_ACTOR_NAME)

  "A BlockPoolActor" should {
    "Respond with a Seq of Blocks." in {
      BlockPoolActor.props shouldEqual Props[BlockPoolActor]

      blockPoolActor ! GetBlocks
      expectMsg(Seq.empty[Block])

      blockPoolActor ! GetBlocks(Set("somehash"))
      expectMsg(Seq.empty[Block])

      blockPoolActor ! AddBlock(Block.genesisBlock)
      expectMsg(SuccessMsg(s"Block ${Block.genesisBlock.hash} created in the Pool."))

      blockPoolActor ! AddBlock(Block.genesisBlock)
      expectMsg(FailureMsg(s"Block ${Block.genesisBlock.hash} already exists in the Pool."))

      blockPoolActor ! GetBlocks
      expectMsg(Seq(Block.genesisBlock))

      blockPoolActor ! GetBlocks(Set("somehash"))
      expectMsg(Seq.empty[Block])

      blockPoolActor ! GetTxOfBlock("aa", Block.genesisBlock.hash)
      expectMsg(None)

      blockPoolActor ! GetTxOfBlock(Block.genesisBlock.transactions.head.id, "bb")
      expectMsg(None)

      blockPoolActor ! GetTxOfBlock(Block.genesisBlock.transactions.head.id, Block.genesisBlock.hash)
      expectMsg(Some(Block.genesisBlock.transactions.head))

      blockPoolActor ! GetBlocks(Set(Block.genesisBlock.hash))
      expectMsg(Seq(Block.genesisBlock))

      blockPoolActor ! GetBlock(Block.genesisBlock.hash)
      expectMsg(Some(Block.genesisBlock))

      within(15 seconds) {
        blockchainActor ! CreateBlockchain
        expectMsgType[SuccessMsg]
      }

      var actualBlock: Block = Block.genesisBlock
      within(15 seconds) {
        blockPoolActor ! MineAndAddNextBlock("next", Seq.empty[String])
        actualBlock = expectMsgType[Some[Block]].get
        actualBlock.data shouldEqual "next"
        actualBlock.transactions shouldEqual Seq.empty[Transaction]
        actualBlock.index shouldEqual 1
        actualBlock.hasValidHash shouldEqual true
      }

      blockPoolActor ! GetBlocks
      val blocks = expectMsgType[Seq[Block]]

      blocks.length shouldEqual 2
      blocks should contain allOf (Block.genesisBlock, actualBlock)

      within(15 seconds) {
        blockchainActor ! DeleteBlockchain
        expectMsg(SuccessMsg("Blockchain deleted."))
      }

      within(15 seconds) {
        blockPoolActor ! MineAndAddNextBlock("next", Seq.empty[String])
        expectMsg(None)
      }

      blockPoolActor ! DeleteBlock(Block.genesisBlock.hash)
      expectMsg(SuccessMsg(s"Block ${Block.genesisBlock.hash} deleted from the Pool."))

      blockPoolActor ! DeleteBlock(Block.genesisBlock.hash)
      expectMsg(FailureMsg(s"Block ${Block.genesisBlock.hash} does not exist in the Pool."))

      blockPoolActor ! GetBlock(Block.genesisBlock.hash)
      expectMsg(None)

      blockPoolActor ! "other"
      expectNoMessage
    }
  }



}
