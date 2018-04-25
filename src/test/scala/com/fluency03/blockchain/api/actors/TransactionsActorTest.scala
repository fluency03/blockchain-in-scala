package com.fluency03.blockchain.api.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.fluency03.blockchain.api.actors.TransactionsActor._
import com.fluency03.blockchain.api.{FailureMsg, SuccessMsg}
import com.fluency03.blockchain.core.Transaction
import com.fluency03.blockchain.core.Transaction.createCoinbaseTx
import com.fluency03.blockchain.{genesisMiner, genesisTimestamp}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class TransactionsActorTest extends TestKit(ActorSystem("TransactionsActorTest")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll: Unit = {
    shutdown()
  }

  val transActor: ActorRef = system.actorOf(Props[TransactionsActor])

  "A TransactionsActor" should {
    "Respond with a Seq of Transactions." in {
      TransactionsActor.props shouldEqual Props[TransactionsActor]

      transActor ! GetTransactions
      expectMsg(Seq.empty[Transaction])

      val genesisTx: Transaction = createCoinbaseTx(0, genesisMiner, genesisTimestamp)
      transActor ! CreateTransaction(genesisTx)
      expectMsg(SuccessMsg(s"Transaction ${genesisTx.id} created."))

      transActor ! CreateTransaction(genesisTx)
      expectMsg(FailureMsg(s"Transaction ${genesisTx.id} already exists."))

      transActor ! GetTransactions
      expectMsg(Seq(genesisTx))

      transActor ! GetTransaction(genesisTx.id)
      expectMsg(Some(genesisTx))

      transActor ! DeleteTransaction(genesisTx.id)
      expectMsg(SuccessMsg(s"Transaction ${genesisTx.id} deleted."))

      transActor ! DeleteTransaction(genesisTx.id)
      expectMsg(FailureMsg(s"Transaction ${genesisTx.id} does not exist."))

      transActor ! GetTransaction(genesisTx.id)
      expectMsg(None)

      transActor ! "other"
      expectNoMessage
    }
  }

}
