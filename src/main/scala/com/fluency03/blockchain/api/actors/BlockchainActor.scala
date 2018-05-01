package com.fluency03.blockchain.api.actors

import akka.actor.{ActorSelection, Props}
import com.fluency03.blockchain.api.actors.BlockchainActor._
import com.fluency03.blockchain.api._
import com.fluency03.blockchain.core.{Block, Blockchain, Transaction}

import scala.collection.mutable

object BlockchainActor {
  final case object GetBlockchain
  final case object CreateBlockchain
  final case object DeleteBlockchain
  final case class GetBlock(hash: String)
  final case class GetTxOfBlock(id: String, hash: String)
  final case class AddBlock(block: Block)
  final case object RemoveBlock
  final case class MineNextBlock(data: String, trans: Seq[Transaction])
  final case object CheckBlockchainValidity

  def props: Props = Props[BlockchainActor]
}

class BlockchainActor extends ActorSupport {
  override def preStart(): Unit = log.info("{} started!", this.getClass.getSimpleName)
  override def postStop(): Unit = log.info("{} stopped!", this.getClass.getSimpleName)

  val blockActor: ActorSelection = context.actorSelection(PARENT_UP + BLOCKS_ACTOR_NAME)
  val transActor: ActorSelection = context.actorSelection(PARENT_UP + TRANS_ACTOR_NAME)
  val networkActor: ActorSelection = context.actorSelection(PARENT_UP + NETWORK_ACTOR_NAME)

  // TODO (Chang): need persistence
  var blockchainOpt: Option[Blockchain] = None
  val hashIndexMapping = mutable.Map.empty[String, Int]

  def receive: Receive = {
    case GetBlockchain => onGetBlockchain()
    case CreateBlockchain => onCreateBlockchain()
    case DeleteBlockchain => onDeleteBlockchain()
    case GetBlock(hash) => onGetBlock(hash)
    case GetTxOfBlock(id, hash) => onGetTxOfBlock(id, hash)
    case AddBlock(block) => onAddBlock(block)
    case RemoveBlock => onRemoveBlock()
    case MineNextBlock(data, trans) => onMineNextBlock(data, trans)
    case CheckBlockchainValidity => onCheckBlockchainValidity()
    case _ => unhandled _
  }

  /**
   * TODO (Chang): new APIS:
   *  - AddBlock with Block obtained from pool based on hash
   *  - MineNextBlock with Transactions
   */

  private def onGetBlockchain(): Unit = sender() ! blockchainOpt

  private def onCreateBlockchain(): Unit =
    if (blockchainOpt.isDefined) sender() ! FailureMsg("Blockchain already exists.")
    else {
      blockchainOpt = Some(Blockchain())
      if (hashIndexMapping.nonEmpty) {
        log.error("Hash-to-index mapping is not empty when Blockchain is created! Clear it!")
        hashIndexMapping.clear()
      }
      blockchainOpt.get.chain.zipWithIndex.foreach { case (b, i) => hashIndexMapping += (b.hash -> i) }
      sender() ! SuccessMsg(s"Blockchain created, with difficulty ${blockchainOpt.get.difficulty}.")
    }

  private def onDeleteBlockchain(): Unit =
    if (blockchainOpt.isDefined) {
      blockchainOpt = None
      hashIndexMapping.clear()
      sender() ! SuccessMsg("Blockchain deleted.")
    } else sender() ! FailureMsg("Blockchain does not exist.")

  private def onGetBlock(hash: String): Unit = sender() ! getBlock(hash)

  private def onGetTxOfBlock(id: String, hash: String): Unit = sender() ! {
    getBlock(hash) match {
      case Some(block) => block.transactions.find(_.id == id)
      case None => None
    }
  }

  private def onAddBlock(block: Block): Unit = blockchainOpt match {
    case Some(blockchain) =>
      blockchainOpt = Some(blockchain.addBlock(block))
      hashIndexMapping += (block.hash -> blockchain.length)
      sender() ! SuccessMsg(s"New Block ${block.hash} added on the chain.")
    case None =>
      log.error("Blockchain does not exist! Clear the hash-to-index mapping!")
      hashIndexMapping.clear()
      sender() ! FailureMsg("Blockchain does not exist.")
  }

  private def onRemoveBlock(): Unit = blockchainOpt match {
    case Some(blockchain) =>
      val toBeRemoved = blockchain.chain.head
      blockchainOpt = Some(blockchain.removeBlock())
      hashIndexMapping -= toBeRemoved.hash
      sender() ! SuccessMsg(s"Last Block ${toBeRemoved.hash} removed from the chain.")
    case None =>
      log.error("Blockchain does not exist! Clear the hash-to-index mapping!")
      hashIndexMapping.clear()
      sender() ! FailureMsg("Blockchain does not exist.")
  }

  private def onMineNextBlock(data: String, trans: Seq[Transaction]): Unit = blockchainOpt match {
    case Some(blockchain) => sender() ! Some(blockchain.mineNextBlock(data, trans))
    case None =>
      log.error("Blockchain does not exist! Clear the hash-to-index mapping!")
      hashIndexMapping.clear()
      sender() ! None
  }

  private def onCheckBlockchainValidity(): Unit = blockchainOpt match {
    case Some(blockchain) =>
      if (blockchain.isValid) sender() ! SuccessMsg("true")
      else sender() ! SuccessMsg("false")
    case None =>
      log.error("Blockchain does not exist! Clear the hash-to-index mapping!")
      hashIndexMapping.clear()
      sender() ! FailureMsg("Blockchain does not exist.")
  }


  private def getBlock(hash: String): Option[Block] = hashIndexMapping.get(hash) match {
    case Some(index) => blockchainOpt match {
      case Some(blockchain) => Some(blockchain.chain(index))
      case None =>
        log.error("Blockchain does not exist! Clear the hash-to-index mapping!")
        hashIndexMapping.clear()
        None
    }
    case None => None
  }

}
