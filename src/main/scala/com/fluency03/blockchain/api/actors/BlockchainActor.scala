package com.fluency03.blockchain.api.actors

import akka.actor.{Actor, ActorLogging, ActorSelection, Props}
import com.fluency03.blockchain.api.actors.BlockchainActor._
import com.fluency03.blockchain.api.actors.TransactionActor._
import com.fluency03.blockchain.api.utils.GenericMessage._
import com.fluency03.blockchain.api.{BLOCK_ACTOR_NAME, PARENT_UP, TX_ACTOR_NAME}
import com.fluency03.blockchain.core.{Blockchain, Transaction}

object BlockchainActor {
  final case object GetBlockchain
  final case object CreateBlockchain
  final case object DeleteBlockchain

  def props: Props = Props[BlockchainActor]
}

class BlockchainActor extends Actor with ActorLogging {
  override def preStart(): Unit = log.info("{} started!", this.getClass.getSimpleName)
  override def postStop(): Unit = log.info("{} stopped!", this.getClass.getSimpleName)

  val txActor: ActorSelection = context.actorSelection(PARENT_UP + TX_ACTOR_NAME)
  val blockActor: ActorSelection = context.actorSelection(PARENT_UP + BLOCK_ACTOR_NAME)

  var blockchainOpt: Option[Blockchain] = None

  def receive: Receive = {
    /* ---------- Blockchain actions ---------- */
    case GetBlockchain => onGetBlockchain()
    case CreateBlockchain => onCreateBlockchain()
    case DeleteBlockchain => onDeleteBlockchain()
    /* ---------- Transaction actions ---------- */
    case GetTransactions => onGetTransactions()
    case CreateTransaction(tx) => onCreateTransaction(tx)
    case GetTransaction(hash) => onGetTransaction(hash)
    case DeleteTransaction(hash) => onDeleteTransaction(hash)
  }

  /* ---------- Blockchain actions ---------- */
  private[this] def onGetBlockchain(): Unit = sender() ! blockchainOpt

  private[this] def onCreateBlockchain(): Unit =
    if (blockchainOpt.isDefined) sender() ! Response(s"Blockchain already exists.")
    else {
      blockchainOpt = Some(Blockchain())
      sender() ! Response(s"Blockchain created, with difficulty ${blockchainOpt.get.difficulty}.")
    }

  private[this] def onDeleteBlockchain(): Unit =
    if (blockchainOpt.isDefined) {
      blockchainOpt = None
      sender() ! Response(s"Blockchain deleted.")
    } else sender() ! Response(s"Blockchain does not exist.")

  /* ---------- Transaction actions ---------- */
  private[this] def onGetTransactions(): Unit = sender() ! {
    if (blockchainOpt.isDefined) blockchainOpt.get.currentTransactions.toList
    else List()
  }

  private[this] def onCreateTransaction(tx: Transaction): Unit =
    if (blockchainOpt.isDefined) {
      blockchainOpt.get.currentTransactions += (tx.hash -> tx)
      sender() ! Response(s"Transaction ${tx.hash} created.")
    } else sender() ! Response(s"Blockchain does not exist.")

  private[this] def onGetTransaction(hash: String): Unit = sender() ! {
    if (blockchainOpt.isDefined) blockchainOpt.get.currentTransactions(hash)
    else None
  }

  private[this] def onDeleteTransaction(hash: String): Unit =
    if (blockchainOpt.isDefined) {
      blockchainOpt.get.currentTransactions -= hash
      sender() ! Response(s"Transaction $hash deleted.")
    } else sender() ! Response(s"Blockchain does not exist.")

}
