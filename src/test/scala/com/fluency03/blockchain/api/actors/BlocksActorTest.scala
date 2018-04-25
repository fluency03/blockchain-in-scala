package com.fluency03.blockchain.api.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.fluency03.blockchain.api.actors.BlocksActor._
import com.fluency03.blockchain.api.{FailureMsg, SuccessMsg}
import com.fluency03.blockchain.core.Block
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class BlocksActorTest extends TestKit(ActorSystem("BlocksActorTest")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll: Unit = {
    shutdown()
  }

  val blocksActor: ActorRef = system.actorOf(Props[BlocksActor])

  "A BlocksActor" should {
    "Respond with a Seq of Blocks." in {
      BlocksActor.props shouldEqual Props[BlocksActor]

      blocksActor ! GetBlocks
      expectMsg(Seq.empty[Block])

      blocksActor ! CreateBlock(Block.genesisBlock)
      expectMsg(SuccessMsg(s"Block ${Block.genesisBlock.hash} created."))

      blocksActor ! CreateBlock(Block.genesisBlock)
      expectMsg(FailureMsg(s"Block ${Block.genesisBlock.hash} already exists."))

      blocksActor ! GetBlocks
      expectMsg(Seq(Block.genesisBlock))

      blocksActor ! GetBlock(Block.genesisBlock.hash)
      expectMsg(Some(Block.genesisBlock))

      blocksActor ! DeleteBlock(Block.genesisBlock.hash)
      expectMsg(SuccessMsg(s"Block ${Block.genesisBlock.hash} deleted."))

      blocksActor ! DeleteBlock(Block.genesisBlock.hash)
      expectMsg(FailureMsg(s"Block ${Block.genesisBlock.hash} does not exist."))

      blocksActor ! GetBlock(Block.genesisBlock.hash)
      expectMsg(None)

      blocksActor ! "other"
      expectNoMessage
    }
  }



}
