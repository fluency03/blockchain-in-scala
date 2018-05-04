package com.github.fluency03.blockchain.api.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.github.fluency03.blockchain.api.actors.PeerActor.{CreateWallet, GetPublicKeys}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class PeerActorTest extends TestKit(ActorSystem("PeerActorTest")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll: Unit = {
    shutdown()
  }

  val peerActor: ActorRef = system.actorOf(Props[PeerActor])

  "A BlocksActor" should {
    "Respond with a Seq of Blocks." in {
      PeerActor.props shouldEqual Props[PeerActor]

      peerActor ! GetPublicKeys
      val publicKeys = expectMsgType[Set[String]]
      publicKeys.size shouldEqual 1

      peerActor ! CreateWallet
      expectMsgType[String]

      peerActor ! GetPublicKeys
      val publicKeys2 = expectMsgType[Set[String]]
      publicKeys2.size shouldEqual 2

      peerActor ! "other"
      expectNoMessage
    }
  }

}
