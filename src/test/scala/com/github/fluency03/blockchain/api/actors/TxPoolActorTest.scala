package com.github.fluency03.blockchain.api.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.github.fluency03.blockchain.api.actors.TxPoolActor._
import com.github.fluency03.blockchain.api.{FailureMsg, SuccessMsg}
import com.github.fluency03.blockchain.core.{Transaction, TxIn, TxOut}
import com.github.fluency03.blockchain.core.Transaction.{createCoinbaseTx, hashOfTransaction}
import com.github.fluency03.blockchain.{genesisMiner, genesisTimestamp}
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

      txPoolActor ! GetTransactions(Seq("someid"))
      expectMsg(Seq.empty[Transaction])

      val genesisTx: Transaction = createCoinbaseTx(0, genesisMiner, genesisTimestamp)
      txPoolActor ! AddTransaction(genesisTx)
      expectMsg(SuccessMsg(s"Transaction ${genesisTx.id} created in the Pool."))

      txPoolActor ! AddTransaction(genesisTx)
      expectMsg(FailureMsg(s"Transaction ${genesisTx.id} already exists in the Pool."))

      txPoolActor ! GetTransactions
      expectMsg(Seq(genesisTx))

      val tx2: Transaction = createCoinbaseTx(2, genesisMiner, genesisTimestamp)
      val tx3: Transaction = createCoinbaseTx(3, genesisMiner, genesisTimestamp)

      txPoolActor ! AddTransaction(tx2)
      expectMsg(SuccessMsg(s"Transaction ${tx2.id} created in the Pool."))

      txPoolActor ! AddTransaction(tx3)
      expectMsg(SuccessMsg(s"Transaction ${tx3.id} created in the Pool."))

      txPoolActor ! GetTransactions(Seq("someid"))
      expectMsg(Seq.empty[Transaction])

      txPoolActor ! GetTransactions(Seq(genesisTx.id))
      expectMsg(Seq(genesisTx))

      txPoolActor ! GetTransactions(Seq(genesisTx.id, tx2.id, tx3.id))
      expectMsg(Seq(genesisTx, tx2, tx3))

      txPoolActor ! GetTransaction(genesisTx.id)
      expectMsg(Some(genesisTx))

      txPoolActor ! UpdateTransaction(genesisTx)
      expectMsg(SuccessMsg(s"Transaction ${genesisTx.id} updated in the Pool."))

      val tx1: Transaction = createCoinbaseTx(1, genesisMiner, genesisTimestamp + 10)
      txPoolActor ! UpdateTransaction(tx1)
      expectMsg(SuccessMsg(s"Transaction ${tx1.id} does not exist. New transaction created in the Pool."))

      val tx0: Transaction = Transaction(Seq.empty[TxIn], Seq.empty[TxOut], genesisTimestamp, "0000")
      val idOfTx0 = hashOfTransaction(tx0)
      txPoolActor ! UpdateTransaction(tx0)
      expectMsg(FailureMsg(s"Transaction does not have valid ID. Should be: $idOfTx0; actually is: ${tx0.id}"))

      txPoolActor ! DeleteTransaction(genesisTx.id)
      expectMsg(SuccessMsg(s"Transaction ${genesisTx.id} deleted from the Pool."))

      txPoolActor ! DeleteTransaction(genesisTx.id)
      expectMsg(FailureMsg(s"Transaction ${genesisTx.id} does not exist in the Pool."))

      txPoolActor ! GetTransaction(genesisTx.id)
      expectMsg(None)

      txPoolActor ! "other"
      expectNoMessage
    }
  }

}
