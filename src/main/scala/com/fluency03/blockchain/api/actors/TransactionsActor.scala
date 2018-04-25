package com.fluency03.blockchain.api.actors

import akka.actor.{ActorSelection, Props}
import com.fluency03.blockchain.api.actors.TransactionsActor._
import com.fluency03.blockchain.api._
import com.fluency03.blockchain.core.{Outpoint, Transaction, TxOut}

import scala.collection.mutable

object TransactionsActor {
  final case object GetTransactions
  final case class CreateTransaction(tx: Transaction)
  final case class GetTransaction(hash: String)
  final case class DeleteTransaction(hash: String)

  def props: Props = Props[TransactionsActor]
}

class TransactionsActor extends Actors {
  override def preStart(): Unit = log.info("{} started!", this.getClass.getSimpleName)
  override def postStop(): Unit = log.info("{} stopped!", this.getClass.getSimpleName)

  // TODO (Chang): need persistence
  val currentTransactions: mutable.Map[String, Transaction] = mutable.Map.empty[String, Transaction]
  val unspentTxOuts: mutable.Map[Outpoint, TxOut] = mutable.Map.empty[Outpoint, TxOut]

  val blockchainActor: ActorSelection = context.actorSelection(PARENT_UP + BLOCKCHAIN_ACTOR_NAME)
  val blockActor: ActorSelection = context.actorSelection(PARENT_UP + BLOCKS_ACTOR_NAME)
  val networkActor: ActorSelection = context.actorSelection(PARENT_UP + NETWORK_ACTOR_NAME)

  def receive: Receive = {
    case GetTransactions => onGetTransactions()
    case CreateTransaction(tx) => onCreateTransaction(tx)
    case GetTransaction(hash) => onGetTransaction(hash)
    case DeleteTransaction(hash) => onDeleteTransaction(hash)
    case _ => unhandled _
  }

  private def onGetTransactions(): Unit = sender() ! currentTransactions.values.toSeq

  private def onCreateTransaction(tx: Transaction): Unit = {
    if (currentTransactions.contains(tx.id)) sender() ! FailureMsg(s"Transaction ${tx.id} already exists.")
    else {
      currentTransactions += (tx.id -> tx)
      sender() ! SuccessMsg(s"Transaction ${tx.id} created.")
    }
  }

  private def onGetTransaction(id: String): Unit = sender() ! currentTransactions.get(id)

  private def onDeleteTransaction(id: String): Unit =
    if (currentTransactions contains id) {
      currentTransactions -= id
      sender() ! SuccessMsg(s"Transaction $id deleted.")
    } else sender() ! FailureMsg(s"Transaction $id does not exist.")

  // TODO (Chang): APIs for selecting transactions based on Seq of ids


}
