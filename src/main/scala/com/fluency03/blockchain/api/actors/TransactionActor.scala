package com.fluency03.blockchain.api.actors

import akka.actor.{Actor, ActorLogging, ActorSelection, Props}
import com.fluency03.blockchain.api.actors.TransactionActor._
import com.fluency03.blockchain.api.utils.GenericMessage.Response
import com.fluency03.blockchain.api.{BLOCKCHAIN_ACTOR_NAME, BLOCK_ACTOR_NAME, PARENT_UP}
import com.fluency03.blockchain.core.{Outpoint, Transaction, TxOut}

import scala.collection.mutable

object TransactionActor {
  final case object GetTransactions
  final case class CreateTransaction(tx: Transaction)
  final case class GetTransaction(hash: String)
  final case class DeleteTransaction(hash: String)

  def props: Props = Props[TransactionActor]
}

class TransactionActor extends Actor with ActorLogging {
  override def preStart(): Unit = log.info("{} started!", this.getClass.getSimpleName)
  override def postStop(): Unit = log.info("{} stopped!", this.getClass.getSimpleName)

  val currentTransactions: mutable.Map[String, Transaction] = mutable.Map.empty[String, Transaction]
  val unspentTxOuts: mutable.Map[Outpoint, TxOut] = mutable.Map.empty[Outpoint, TxOut]

  // TODO (Chang): not persistent
  val blockchainActor: ActorSelection = context.actorSelection(PARENT_UP + BLOCKCHAIN_ACTOR_NAME)
  val blockActor: ActorSelection = context.actorSelection(PARENT_UP + BLOCK_ACTOR_NAME)

  def receive: Receive = {
    case GetTransactions => onGetTransactions()
    case CreateTransaction(tx) => onCreateTransaction(tx)
    case GetTransaction(hash) => onGetTransaction(hash)
    case DeleteTransaction(hash) => onDeleteTransaction(hash)
    case _ => unhandled _
  }

  private def onGetTransactions(): Unit = sender() ! currentTransactions.values.toList

  private def onCreateTransaction(tx: Transaction): Unit ={
    currentTransactions += (tx.id -> tx)
    sender() ! Response(s"Transaction ${tx.id} created.")
  }

  private def onGetTransaction(hash: String): Unit = sender() ! currentTransactions.get(hash)

  private def onDeleteTransaction(hash: String): Unit =
    if (currentTransactions contains hash) {
      currentTransactions -= hash
      sender() ! Response(s"Transaction $hash deleted.")
    } else sender() ! Response(s"Blockchain does not exist.")


}
