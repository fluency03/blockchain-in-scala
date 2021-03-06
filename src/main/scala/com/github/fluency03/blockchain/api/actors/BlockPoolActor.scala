package com.github.fluency03.blockchain
package api.actors

import akka.actor.{ActorRef, ActorSelection, Props}
import akka.pattern.ask
import com.github.fluency03.blockchain.api.actors.BlockPoolActor._
import com.github.fluency03.blockchain.api._
import com.github.fluency03.blockchain.core.Block

import scala.collection.mutable
import scala.util.{Failure, Success}

object BlockPoolActor {
  final case object GetBlocks
  final case class GetBlocks(hashes: Set[HexString])
  final case class AddBlock(block: Block)
  final case class GetBlock(hash: HexString)
  final case class DeleteBlock(hash: HexString)
  final case class GetTxOfBlock(id: HexString, hash: HexString)
  final case class MineAndAddNextBlock(data: String, ids: Seq[HexString])

  def props: Props = Props[BlockPoolActor]
}

class BlockPoolActor extends ActorSupport {
  override def preStart(): Unit = log.info("{} started!", this.getClass.getSimpleName)
  override def postStop(): Unit = log.info("{} stopped!", this.getClass.getSimpleName)

  import context.dispatcher

  val blockchainActor: ActorSelection = context.actorSelection(PARENT_UP + BLOCKCHAIN_ACTOR_NAME)
  val networkActor: ActorSelection = context.actorSelection(PARENT_UP + NETWORK_ACTOR_NAME)
  val txPoolActor: ActorSelection = context.actorSelection(PARENT_UP + TX_POOL_ACTOR_NAME)

  // TODO (Chang): need persistence
  var blocksPool: mutable.Map[HexString, Block] = mutable.Map.empty[HexString, Block]

  def receive: Receive = {
    case GetBlocks => onGetBlocks()
    case GetBlocks(hashes) => onGetBlocks(hashes)
    case AddBlock(block) => onAddBlock(block)
    case GetBlock(hash) => onGetBlock(hash)
    case DeleteBlock(hash) => onDeleteBlock(hash)
    case GetTxOfBlock(id, hash) => onGetTxOfBlock(id, hash)
    case MineAndAddNextBlock(data, ids) => onMineAndAddNextBlock(data, ids)
    case _ => unhandled _
  }

  /**
   * Handlers for each of the Messages.
   */
  private[this] def onGetBlocks(): Unit = sender() ! blocksPool.values.toSeq

  private[this] def onGetBlocks(hashes: Set[HexString]): Unit =
    sender() ! blocksPool.filterKeys(hashes.contains).values.toSeq

  private[this] def onAddBlock(block: Block): Unit = {
    if (blocksPool.contains(block.hash))
      sender() ! FailureMsg(s"Block ${block.hash} already exists in the Pool.")
    else {
      blocksPool += (block.hash -> block)
      sender() ! SuccessMsg(s"Block ${block.hash} created in the Pool.")
    }
  }

  private[this] def onGetBlock(hash: HexString): Unit = sender() ! blocksPool.get(hash)

  private[this] def onDeleteBlock(hash: HexString): Unit =
    if (blocksPool.contains(hash)) {
      blocksPool -= hash
      sender() ! SuccessMsg(s"Block $hash deleted from the Pool.")
    } else sender() ! FailureMsg(s"Block $hash does not exist in the Pool.")

  private def onGetTxOfBlock(id: HexString, hash: HexString): Unit = blocksPool.get(hash) match {
    case Some(block) => sender() ! block.transactions.find(_.id == id)
    case None => sender() ! None
  }

  private def onMineAndAddNextBlock(data: String, ids: Seq[HexString]): Unit = {
    val theSender: ActorRef = sender()
    (blockchainActor ? BlockchainActor.MineNextBlock(data, ids))
      .mapTo[Option[Block]]
      .onComplete {
        case Success(blockOpt) => blockOpt match {
          case Some(block) =>
            blocksPool += (block.hash -> block)
            theSender ! Some(block)
          case None => theSender ! None
        }
        case Failure(_) => theSender ! None
      }
  }


}
