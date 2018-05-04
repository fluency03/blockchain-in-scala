package com.fluency03.blockchain.api.actors

import akka.actor.{ActorSelection, Props}
import com.fluency03.blockchain.api.actors.BlockPoolActor._
import com.fluency03.blockchain.api._
import com.fluency03.blockchain.core.Block

import scala.collection.mutable

object BlockPoolActor {
  final case object GetBlocks
  final case class GetBlocks(hashes: Set[String])
  final case class CreateBlock(block: Block)
  final case class GetBlock(hash: String)
  final case class DeleteBlock(hash: String)

  def props: Props = Props[BlockPoolActor]
}

class BlockPoolActor extends ActorSupport {
  override def preStart(): Unit = log.info("{} started!", this.getClass.getSimpleName)
  override def postStop(): Unit = log.info("{} stopped!", this.getClass.getSimpleName)

  val blockchainActor: ActorSelection = context.actorSelection(PARENT_UP + BLOCKCHAIN_ACTOR_NAME)
  val networkActor: ActorSelection = context.actorSelection(PARENT_UP + NETWORK_ACTOR_NAME)
  val txPoolActor: ActorSelection = context.actorSelection(PARENT_UP + TX_POOL_ACTOR_NAME)

  // TODO (Chang): need persistence
  var blocksPool = mutable.Map.empty[String, Block]

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
   *  - ContainsBlock
   *  - AddBlockOnChain
   *
   */

  private[this] def onGetBlocks(): Unit = sender() ! blocksPool.values.toSeq

  private[this] def onGetBlocks(hashes: Set[String]): Unit = sender() ! blocksPool.filterKeys(
    k => hashes.contains(k)
  ).values.toSeq

  private[this] def onCreateBlock(block: Block): Unit = {
    if (blocksPool.contains(block.hash)) sender() ! FailureMsg(s"Block ${block.hash} already exists.")
    else {
      blocksPool += (block.hash -> block)
      sender() ! SuccessMsg(s"Block ${block.hash} created.")
    }
  }

  private[this] def onGetBlock(hash: String): Unit = sender() ! blocksPool.get(hash)

  private[this] def onDeleteBlock(hash: String): Unit =
    if (blocksPool.contains(hash)) {
      blocksPool -= hash
      sender() ! SuccessMsg(s"Block $hash deleted.")
    } else sender() ! FailureMsg(s"Block $hash does not exist.")

}