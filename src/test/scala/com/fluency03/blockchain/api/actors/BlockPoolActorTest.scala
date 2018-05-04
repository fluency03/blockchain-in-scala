package com.fluency03.blockchain.api.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.fluency03.blockchain.api.actors.BlockPoolActor._
import com.fluency03.blockchain.api.{FailureMsg, SuccessMsg}
import com.fluency03.blockchain.core.Block
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class BlockPoolActorTest extends TestKit(ActorSystem("BlocksActorTest")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll: Unit = {
    shutdown()
  }

  val blockPoolActor: ActorRef = system.actorOf(Props[BlockPoolActor])

  "A BlocksActor" should {
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

      blockPoolActor ! GetBlocks(Set(Block.genesisBlock.hash))
      expectMsg(Seq(Block.genesisBlock))

      blockPoolActor ! GetBlock(Block.genesisBlock.hash)
      expectMsg(Some(Block.genesisBlock))

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
