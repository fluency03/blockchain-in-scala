package com.fluency03.blockchain.api.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.fluency03.blockchain.api.actors.TxPoolActor._
import com.fluency03.blockchain.api.{FailureMsg, SuccessMsg}
import com.fluency03.blockchain.core.{Transaction, TxIn, TxOut}
import com.fluency03.blockchain.core.Transaction.{createCoinbaseTx, hashOfTransaction}
import com.fluency03.blockchain.{genesisMiner, genesisTimestamp}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class TxPoolActorTest extends TestKit(ActorSystem("TransactionsActorTest")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll: Unit = {
    shutdown()
  }

  val txPoolActor: ActorRef = system.actorOf(Props[TxPoolActor])

  "A TransactionsActor" should {
    "Respond with a Seq of Transactions." in {
      TxPoolActor.props shouldEqual Props[TxPoolActor]

      txPoolActor ! GetTransactions
      expectMsg(Seq.empty[Transaction])

      txPoolActor ! GetTransactions(Set("someid"))
      expectMsg(Seq.empty[Transaction])

      val genesisTx: Transaction = createCoinbaseTx(0, genesisMiner, genesisTimestamp)
      txPoolActor ! CreateTransaction(genesisTx)
      expectMsg(SuccessMsg(s"Transaction ${genesisTx.id} created."))

      txPoolActor ! CreateTransaction(genesisTx)
      expectMsg(FailureMsg(s"Transaction ${genesisTx.id} already exists."))

      txPoolActor ! GetTransactions
      expectMsg(Seq(genesisTx))

      txPoolActor ! GetTransactions(Set("someid"))
      expectMsg(Seq.empty[Transaction])

      txPoolActor ! GetTransactions(Set(genesisTx.id))
      expectMsg(Seq(genesisTx))

      txPoolActor ! GetTransaction(genesisTx.id)
      expectMsg(Some(genesisTx))

      txPoolActor ! UpdateTransaction(genesisTx)
      expectMsg(SuccessMsg(s"Transaction ${genesisTx.id} updated."))

      val tx1: Transaction = createCoinbaseTx(1, genesisMiner, genesisTimestamp + 10)
      txPoolActor ! UpdateTransaction(tx1)
      expectMsg(SuccessMsg(s"Transaction ${tx1.id} does not exist. New transaction created."))

      val tx0: Transaction = Transaction(Seq.empty[TxIn], Seq.empty[TxOut], genesisTimestamp, "0000")
      val idOfTx0 = hashOfTransaction(tx0)
      txPoolActor ! UpdateTransaction(tx0)
      expectMsg(FailureMsg(s"Transaction does not have valid ID. Should be: $idOfTx0; actually is: ${tx0.id}"))

      txPoolActor ! DeleteTransaction(genesisTx.id)
      expectMsg(SuccessMsg(s"Transaction ${genesisTx.id} deleted."))

      txPoolActor ! DeleteTransaction(genesisTx.id)
      expectMsg(FailureMsg(s"Transaction ${genesisTx.id} does not exist."))

      txPoolActor ! GetTransaction(genesisTx.id)
      expectMsg(None)

      txPoolActor ! "other"
      expectNoMessage
    }
  }

}
