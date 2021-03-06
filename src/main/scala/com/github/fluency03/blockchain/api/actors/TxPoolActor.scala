package com.github.fluency03.blockchain
package api.actors

import akka.actor.{ActorSelection, Props}
import com.github.fluency03.blockchain.api.actors.TxPoolActor._
import com.github.fluency03.blockchain.api._
import com.github.fluency03.blockchain.core.{Outpoint, Transaction, TxOut}
import com.github.fluency03.blockchain.core.Transaction.hashOfTransaction

import scala.collection.mutable

object TxPoolActor {
  final case object GetTransactions
  final case class GetTransactions(ids: Seq[HexString])
  final case class AddTransaction(tx: Transaction)
  final case class GetTransaction(id: HexString)
  final case class DeleteTransaction(id: HexString)
  final case class UpdateTransaction(tx: Transaction)
  def props: Props = Props[TxPoolActor]
}

class TxPoolActor extends ActorSupport {
  override def preStart(): Unit = log.info("{} started!", this.getClass.getSimpleName)
  override def postStop(): Unit = log.info("{} stopped!", this.getClass.getSimpleName)

  // TODO (Chang): need persistence
  val transPool: mutable.Map[HexString, Transaction] = mutable.Map.empty[HexString, Transaction]
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

  private def onGetTransactions(ids: Seq[HexString]): Unit =
    sender() ! ids.map(transPool.get).filter(_.isDefined).map(_.get)

  private def onAddTransaction(tx: Transaction): Unit =
    if (transPool.contains(tx.id))
      sender() ! FailureMsg(s"Transaction ${tx.id} already exists in the Pool.")
    else {
      transPool += (tx.id -> tx)
      sender() ! SuccessMsg(s"Transaction ${tx.id} created in the Pool.")
    }

  private def onGetTransaction(id: HexString): Unit = sender() ! transPool.get(id)

  private def onDeleteTransaction(id: HexString): Unit =
    if (transPool.contains(id)) {
      transPool -= id
      sender() ! SuccessMsg(s"Transaction $id deleted from the Pool.")
    } else sender() ! FailureMsg(s"Transaction $id does not exist in the Pool.")

  private def onUpdateTransaction(tx: Transaction): Unit = {
    val (actualId, expectedId) = (tx.id, hashOfTransaction(tx))
    if (actualId != expectedId)
      sender() ! FailureMsg(
        s"Transaction does not have valid ID. Should be: $expectedId; actually is: $actualId")
    else {
      val msg = if (!transPool.contains(actualId))
        SuccessMsg(s"Transaction $actualId does not exist. New transaction created in the Pool.")
      else
        SuccessMsg(s"Transaction $actualId updated in the Pool.")
      transPool += (actualId -> tx)
      sender() ! msg
    }
  }

}
