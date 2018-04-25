package com.fluency03.blockchain.api.actors

import akka.actor.{ActorSelection, Props}
import com.fluency03.blockchain.api.actors.BlockchainActor._
import com.fluency03.blockchain.api._
import com.fluency03.blockchain.core.Blockchain

object BlockchainActor {
  final case object GetBlockchain
  final case object CreateBlockchain
  final case object DeleteBlockchain

  def props: Props = Props[BlockchainActor]
}

class BlockchainActor extends Actors {
  override def preStart(): Unit = log.info("{} started!", this.getClass.getSimpleName)
  override def postStop(): Unit = log.info("{} stopped!", this.getClass.getSimpleName)

  val blockActor: ActorSelection = context.actorSelection(PARENT_UP + BLOCKS_ACTOR_NAME)
  val transActor: ActorSelection = context.actorSelection(PARENT_UP + TRANS_ACTOR_NAME)
  val networkActor: ActorSelection = context.actorSelection(PARENT_UP + NETWORK_ACTOR_NAME)

  // TODO (Chang): need persistence
  var blockchainOpt: Option[Blockchain] = None

  def receive: Receive = {
    case GetBlockchain => onGetBlockchain()
    case CreateBlockchain => onCreateBlockchain()
    case DeleteBlockchain => onDeleteBlockchain()
    case _ => unhandled _
  }

  private def onGetBlockchain(): Unit = sender() ! blockchainOpt

  private def onCreateBlockchain(): Unit =
    if (blockchainOpt.isDefined) sender() ! FailureMsg(s"Blockchain already exists.")
    else {
      blockchainOpt = Some(Blockchain())
      sender() ! SuccessMsg(s"Blockchain created, with difficulty ${blockchainOpt.get.difficulty}.")
    }

  private def onDeleteBlockchain(): Unit =
    if (blockchainOpt.isDefined) {
      blockchainOpt = None
      sender() ! SuccessMsg(s"Blockchain deleted.")
    } else sender() ! FailureMsg(s"Blockchain does not exist.")

  // TODO (Chang): APIs for adding new Block on the chain



}
