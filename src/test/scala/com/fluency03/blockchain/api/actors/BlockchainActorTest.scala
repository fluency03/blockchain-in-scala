package com.fluency03.blockchain.api.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.fluency03.blockchain.api.{FailureMsg, SuccessMsg}
import com.fluency03.blockchain.api.actors.BlockchainActor.{CreateBlockchain, DeleteBlockchain, GetBlockchain}
import com.fluency03.blockchain.core.Blockchain
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

class BlockchainActorTest extends TestKit(ActorSystem("BlockchainActorTest")) with DefaultTimeout with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll  {

  override def afterAll: Unit = {
    shutdown()
  }

  val blockchainActor: ActorRef = system.actorOf(Props[BlockchainActor])

  "A BlockchainActor" should {
    "Respond with a Blockchain." in {
      blockchainActor ! GetBlockchain
      expectMsg(None)

      within(15 seconds) {
        blockchainActor ! CreateBlockchain
        expectMsgType[SuccessMsg]
      }

      blockchainActor ! CreateBlockchain
      expectMsgType[FailureMsg]

      blockchainActor ! GetBlockchain
      expectMsgType[Some[Blockchain]]

      blockchainActor ! DeleteBlockchain
      expectMsg(SuccessMsg(s"Blockchain deleted."))

      blockchainActor ! DeleteBlockchain
      expectMsg(FailureMsg(s"Blockchain does not exist."))

      blockchainActor ! "other"
      expectNoMessage
    }
  }

}
