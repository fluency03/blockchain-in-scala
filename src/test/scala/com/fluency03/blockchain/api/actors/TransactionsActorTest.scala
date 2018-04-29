package com.fluency03.blockchain.api.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.fluency03.blockchain.api.actors.TransactionsActor._
import com.fluency03.blockchain.api.{FailureMsg, SuccessMsg}
import com.fluency03.blockchain.core.{Transaction, TxIn, TxOut}
import com.fluency03.blockchain.core.Transaction.{createCoinbaseTx, hashOfTransaction}
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

      transActor ! GetTransactions(Set("someid"))
      expectMsg(Seq.empty[Transaction])

      val genesisTx: Transaction = createCoinbaseTx(0, genesisMiner, genesisTimestamp)
      transActor ! CreateTransaction(genesisTx)
      expectMsg(SuccessMsg(s"Transaction ${genesisTx.id} created."))

      transActor ! CreateTransaction(genesisTx)
      expectMsg(FailureMsg(s"Transaction ${genesisTx.id} already exists."))

      transActor ! GetTransactions
      expectMsg(Seq(genesisTx))

      transActor ! GetTransactions(Set("someid"))
      expectMsg(Seq.empty[Transaction])

      transActor ! GetTransactions(Set(genesisTx.id))
      expectMsg(Seq(genesisTx))

      transActor ! GetTransaction(genesisTx.id)
      expectMsg(Some(genesisTx))

      transActor ! UpdateTransaction(genesisTx)
      expectMsg(SuccessMsg(s"Transaction ${genesisTx.id} updated."))

      val tx1: Transaction = createCoinbaseTx(1, genesisMiner, genesisTimestamp + 10)
      transActor ! UpdateTransaction(tx1)
      expectMsg(SuccessMsg(s"Transaction ${tx1.id} does not exist. New transaction created."))

      val tx0: Transaction = Transaction(Seq.empty[TxIn], Seq.empty[TxOut], genesisTimestamp, "0000")
      val idOfTx0 = hashOfTransaction(tx0)
      transActor ! UpdateTransaction(tx0)
      expectMsg(FailureMsg(s"Transaction does not have valid ID. Should be: $idOfTx0; actually is: ${tx0.id}"))

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
