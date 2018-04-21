package com.fluency03.blockchain.api.actors

import akka.actor.{Actor, ActorLogging, ActorSelection, Props}
import com.fluency03.blockchain.api.actors.BlockActor._
import com.fluency03.blockchain.api.utils.GenericMessage._
import com.fluency03.blockchain.api.{BLOCKCHAIN_ACTOR_NAME, PARENT_UP, TX_ACTOR_NAME}
import com.fluency03.blockchain.core.Block
import scala.collection.mutable

object BlockActor {
  final case object GetBlocks
  final case class CreateBlock(block: Block)
  final case class GetBlock(hash: String)
  final case class DeleteBlock(hash: String)

  def props: Props = Props[BlockActor]
}

class BlockActor extends Actor with ActorLogging {
  override def preStart(): Unit = log.info("{} started!", this.getClass.getSimpleName)
  override def postStop(): Unit = log.info("{} stopped!", this.getClass.getSimpleName)

  val blockchainActor: ActorSelection = context.actorSelection(PARENT_UP + BLOCKCHAIN_ACTOR_NAME)
  val txActor: ActorSelection = context.actorSelection(PARENT_UP + TX_ACTOR_NAME)

  var blocks = mutable.Map.empty[String, Block]

  def receive: Receive = {
    case GetBlocks => onGetBlocks()
    case CreateBlock(block) => onCreateBlock(block)
    case GetBlock(hash) => onGetBlock(hash)
    case DeleteBlock(hash) => onDeleteBlock(hash)
    case _ => unhandled _
  }

  private[this] def onGetBlocks(): Unit = sender() ! blocks.values.toList

  private[this] def onCreateBlock(block: Block): Unit = {
    blocks += (block.hash -> block)
    sender() ! Response(s"Block ${block.hash} created.")
  }

  private[this] def onGetBlock(hash: String): Unit = sender() ! blocks.get(hash)

  private[this] def onDeleteBlock(hash: String): Unit = {
    blocks -= hash
    sender() ! Response(s"Block $hash deleted.")
  }

}
