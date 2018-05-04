package com.github.fluency03.blockchain.api.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.github.fluency03.blockchain.api.actors.NetworkActor._
import com.github.fluency03.blockchain.api.{FailureMsg, SuccessMsg}
import com.github.fluency03.blockchain.core.Peer
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class NetworkActorTest extends TestKit(ActorSystem("NetworkActorTest")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll: Unit = {
    shutdown()
  }

  val networkActor: ActorRef = system.actorOf(Props[NetworkActor])

  "A NetworkActor" should {
    "Respond with a Set of Peers." in {
      NetworkActor.props shouldEqual Props[NetworkActor]

      networkActor ! GetNetwork
      expectMsg(Set.empty[String])

      networkActor ! GetPeers
      expectMsg(Map.empty[String, Set[String]])

      networkActor ! GetPeers(Set("somename"))
      expectMsg(Map.empty[String, Set[String]])

      val name = "peer"
      networkActor ! CreatePeer(name)
      expectMsg(SuccessMsg(s"Peer $name created."))

      networkActor ! CreatePeer(name)
      expectMsg(FailureMsg(s"Peer $name has been created."))

      networkActor ! GetNetwork
      expectMsg(Set(name))

      networkActor ! GetPeers
      val peers = expectMsgType[Map[String, Set[String]]]
      peers.size shouldEqual 1
      peers.contains(name) shouldEqual true
      peers(name).size shouldEqual 1

      networkActor ! GetPeers(Set("somename"))
      expectMsg(Map.empty[String, Set[String]])

      networkActor ! GetPeers(Set(name))
      val peers2 = expectMsgType[Map[String, Set[String]]]
      peers2 shouldEqual peers

      networkActor ! GetPeer(name)
      val peerOpt = expectMsgType[Some[Peer]]
      peerOpt.isDefined shouldEqual true
      peerOpt.get.name shouldEqual name
      peerOpt.get.publicKeys.size shouldEqual 1

      networkActor ! DeletePeer(name)
      expectMsg(SuccessMsg(s"Peer $name deleted."))

      // wait 1 second for Peer Actor to completely stop
      Thread.sleep(1000)

      networkActor ! DeletePeer(name)
      expectMsg(FailureMsg(s"Peer $name does not exist."))

      networkActor ! GetPeer(name)
      expectMsg(None)

      networkActor ! "other"
      expectNoMessage
    }
  }




}
