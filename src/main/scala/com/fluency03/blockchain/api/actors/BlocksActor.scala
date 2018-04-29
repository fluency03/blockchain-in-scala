package com.fluency03.blockchain.api.actors

import akka.actor.{ActorSelection, Props}
import com.fluency03.blockchain.api.actors.BlocksActor._
import com.fluency03.blockchain.api._
import com.fluency03.blockchain.core.Block

import scala.collection.mutable

object BlocksActor {
  final case object GetBlocks
  final case class GetBlocks(hashes: Set[String])
  final case class CreateBlock(block: Block)
  final case class GetBlock(hash: String)
  final case class DeleteBlock(hash: String)

  def props: Props = Props[BlocksActor]
}

class BlocksActor extends ActorSupport {
  override def preStart(): Unit = log.info("{} started!", this.getClass.getSimpleName)
  override def postStop(): Unit = log.info("{} stopped!", this.getClass.getSimpleName)

  val blockchainActor: ActorSelection = context.actorSelection(PARENT_UP + BLOCKCHAIN_ACTOR_NAME)
  val networkActor: ActorSelection = context.actorSelection(PARENT_UP + NETWORK_ACTOR_NAME)
  val transActor: ActorSelection = context.actorSelection(PARENT_UP + TRANS_ACTOR_NAME)

  // TODO (Chang): need persistence
  var blocks = mutable.Map.empty[String, Block]

  def receive: Receive = {
    case GetBlocks => onGetBlocks()
    case GetBlocks(hashes) => onGetBlocks(hashes)
    case CreateBlock(block) => onCreateBlock(block)
    case GetBlock(hash) => onGetBlock(hash)
    case DeleteBlock(hash) => onDeleteBlock(hash)
    case _ => unhandled _
  }

  /**
   * TODO (Chang): new APIS:
   *  - CreateBlock
   *  - GetBlock (onChain or offChain)
   *  - GetTransactionOfABlock
   *  - AddBlockOnChain
   *
   */

  private[this] def onGetBlocks(): Unit = sender() ! blocks.values.toSeq
  private[this] def onGetBlocks(hashes: Set[String]): Unit = sender() ! blocks.filterKeys(
    k => hashes.contains(k)
  ).values.toSeq

  private[this] def onCreateBlock(block: Block): Unit = {
    if (blocks.contains(block.hash)) sender() ! FailureMsg(s"Block ${block.hash} already exists.")
    else {
      blocks += (block.hash -> block)
      sender() ! SuccessMsg(s"Block ${block.hash} created.")
    }
  }

  private[this] def onGetBlock(hash: String): Unit = sender() ! blocks.get(hash)

  private[this] def onDeleteBlock(hash: String): Unit = {
    if (blocks.contains(hash)) {
      blocks -= hash
      sender() ! SuccessMsg(s"Block $hash deleted.")
    } else sender() ! FailureMsg(s"Block $hash does not exist.")
  }


}
