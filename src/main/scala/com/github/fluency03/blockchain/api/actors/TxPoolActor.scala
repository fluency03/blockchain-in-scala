package com.github.fluency03.blockchain.api.actors

import akka.actor.{ActorSelection, Props}
import com.github.fluency03.blockchain.api.actors.TxPoolActor._
import com.github.fluency03.blockchain.api._
import com.github.fluency03.blockchain.core.{Outpoint, Transaction, TxOut}
import com.github.fluency03.blockchain.core.Transaction.hashOfTransaction

import scala.collection.mutable

object TxPoolActor {
  final case object GetTransactions
  final case class GetTransactions(ids: Seq[String])
  final case class AddTransaction(tx: Transaction)
  final case class GetTransaction(id: String)
  final case class DeleteTransaction(id: String)
  final case class UpdateTransaction(tx: Transaction)
  def props: Props = Props[TxPoolActor]
}

class TxPoolActor extends ActorSupport {
  override def preStart(): Unit = log.info("{} started!", this.getClass.getSimpleName)
  override def postStop(): Unit = log.info("{} stopped!", this.getClass.getSimpleName)

  // TODO (Chang): need persistence
  val transPool: mutable.Map[String, Transaction] = mutable.Map.empty[String, Transaction]
  val uTxOs: mutable.Map[Outpoint, TxOut] = mutable.Map.empty[Outpoint, TxOut]

  val blockchainActor: ActorSelection = context.actorSelection(PARENT_UP + BLOCKCHAIN_ACTOR_NAME)
  val blockPoolActor: ActorSelection = context.actorSelection(PARENT_UP + BLOCK_POOL_ACTOR_NAME)
  val networkActor: ActorSelection = context.actorSelection(PARENT_UP + NETWORK_ACTOR_NAME)

  /**
   * TODO (Chang):
   *  - Sign transaction
   *
   */

  def receive: Receive = {
    case GetTransactions => onGetTransactions()
    case GetTransactions(ids) => onGetTransactions(ids)
    case AddTransaction(tx) => onAddTransaction(tx)
    case GetTransaction(id) => onGetTransaction(id)
    case DeleteTransaction(id) => onDeleteTransaction(id)
    case UpdateTransaction(tx) => onUpdateTransaction(tx)
    case _ => unhandled _
  }

  /**
   * Handlers for each of the Messages.
   */
  private def onGetTransactions(): Unit = sender() ! transPool.values.toSeq

  private def onGetTransactions(ids: Seq[String]): Unit =
    sender() ! ids.map(id => transPool.get(id)).filter(_.isDefined).map(_.get)

  private def onAddTransaction(tx: Transaction): Unit =
    if (transPool.contains(tx.id))
      sender() ! FailureMsg(s"Transaction ${tx.id} already exists in the Pool.")
    else {
      transPool += (tx.id -> tx)
      sender() ! SuccessMsg(s"Transaction ${tx.id} created in the Pool.")
    }

  private def onGetTransaction(id: String): Unit = sender() ! transPool.get(id)

  private def onDeleteTransaction(id: String): Unit =
    if (transPool contains id) {
      transPool -= id
      sender() ! SuccessMsg(s"Transaction $id deleted from the Pool.")
    } else sender() ! FailureMsg(s"Transaction $id does not exist in the Pool.")

  private def onUpdateTransaction(tx: Transaction): Unit = {
    val actualId = tx.id
    val expectedId = hashOfTransaction(tx)
    if (actualId == expectedId) {
      val notExistBefore =  !transPool.contains(actualId)
      transPool += (actualId -> tx)
      sender() ! {
        if (notExistBefore)
          SuccessMsg(s"Transaction $actualId does not exist. New transaction created in the Pool.")
        else
          SuccessMsg(s"Transaction $actualId updated in the Pool.")
      }
    } else sender() ! FailureMsg(s"Transaction does not have valid ID. Should be: $expectedId; actually is: $actualId")
  }


}
