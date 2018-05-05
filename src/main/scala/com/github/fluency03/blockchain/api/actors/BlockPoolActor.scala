package com.github.fluency03.blockchain.api.actors

import akka.actor.{ActorRef, ActorSelection, Props}
import akka.pattern.ask
import com.github.fluency03.blockchain.api.actors.BlockPoolActor._
import com.github.fluency03.blockchain.api._
import com.github.fluency03.blockchain.api.actors.BlockchainActor
import com.github.fluency03.blockchain.core.Block

import scala.collection.mutable
import scala.concurrent.Future
import scala.util.{Failure, Success}

object BlockPoolActor {
  final case object GetBlocks
  final case class GetBlocks(hashes: Set[String])
  final case class AddBlock(block: Block)
  final case class GetBlock(hash: String)
  final case class DeleteBlock(hash: String)
  final case class GetTxOfBlock(id: String, hash: String)
  final case class MineAndAddNextBlock(data: String, ids: Seq[String])

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
    case AddBlock(block) => onAddBlock(block)
    case GetBlock(hash) => onGetBlock(hash)
    case DeleteBlock(hash) => onDeleteBlock(hash)
    case GetTxOfBlock(id, hash) => onGetTxOfBlock(id, hash)
    case MineAndAddNextBlock(data, ids) => onMineAndAddNextBlock(data, ids)
    case _ => unhandled _
  }

  /**
   * TODO (Chang): new APIS:
   *  - CreateBlock
   *
   */

  /**
   * Handlers for each of the Messages.
   */

  private[this] def onGetBlocks(): Unit = sender() ! blocksPool.values.toSeq

  private[this] def onGetBlocks(hashes: Set[String]): Unit = sender() ! blocksPool.filterKeys(
    k => hashes.contains(k)
  ).values.toSeq

  private[this] def onAddBlock(block: Block): Unit = {
    if (blocksPool.contains(block.hash)) sender() ! FailureMsg(s"Block ${block.hash} already exists in the Pool.")
    else {
      blocksPool += (block.hash -> block)
      sender() ! SuccessMsg(s"Block ${block.hash} created in the Pool.")
    }
  }

  private[this] def onGetBlock(hash: String): Unit = sender() ! blocksPool.get(hash)

  private[this] def onDeleteBlock(hash: String): Unit =
    if (blocksPool.contains(hash)) {
      blocksPool -= hash
      sender() ! SuccessMsg(s"Block $hash deleted from the Pool.")
    } else sender() ! FailureMsg(s"Block $hash does not exist in the Pool.")

  private def onGetTxOfBlock(id: String, hash: String): Unit = blocksPool.get(hash) match {
    case Some(block) => sender() ! block.transactions.find(_.id == id)
    case None => sender() ! None
  }

  private def onMineAndAddNextBlock(data: String, ids: Seq[String]): Unit = {
    val maybeBlock: Future[Option[Block]] =
      (blockchainActor ? BlockchainActor.MineNextBlock(data, ids)).mapTo[Option[Block]]
    val theSender: ActorRef = sender()
    maybeBlock onComplete {
      case Success(blockOpt) => blockOpt match {
        case Some(block) =>
          blocksPool += (block.hash -> block)
          Some(block)
        case None => theSender ! None
      }
      case Failure(_) => theSender ! None
    }
  }


}
