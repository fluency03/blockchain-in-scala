package com.fluency03.blockchain.api.actors

import akka.actor.{ActorSelection, Props}
import akka.pattern.ask
import com.fluency03.blockchain.api.actors.BlockchainActor._
import com.fluency03.blockchain.api._
import com.fluency03.blockchain.core.{Block, Blockchain, Transaction}

import scala.collection.mutable
import scala.concurrent.Future
import scala.util.{Failure, Success}

object BlockchainActor {
  final case object GetBlockchain
  final case object CreateBlockchain
  final case object DeleteBlockchain
  final case class GetBlockFromChain(hash: String)
  final case class GetTxOfBlock(id: String, hash: String)
  final case class AddBlock(block: Block)
  final case class AddBlockFromPool(hash: String)
  final case object RemoveBlock
  final case class MineNextBlock(data: String, ids: Seq[String])
  final case object CheckBlockchainValidity
  final case class GetBlockFromPool(hash: String)

  def props: Props = Props[BlockchainActor]
}

class BlockchainActor extends ActorSupport {
  override def preStart(): Unit = log.info("{} started!", this.getClass.getSimpleName)
  override def postStop(): Unit = log.info("{} stopped!", this.getClass.getSimpleName)

  import context.dispatcher

  val blocksActor: ActorSelection = context.actorSelection(PARENT_UP + BLOCKS_ACTOR_NAME)
  val transActor: ActorSelection = context.actorSelection(PARENT_UP + TRANS_ACTOR_NAME)
  val networkActor: ActorSelection = context.actorSelection(PARENT_UP + NETWORK_ACTOR_NAME)

  // TODO (Chang): need persistence
  var blockchainOpt: Option[Blockchain] = None
  val hashIndexMapping = mutable.Map.empty[String, Int]

  def receive: Receive = {
    case GetBlockchain => onGetBlockchain()
    case CreateBlockchain => onCreateBlockchain()
    case DeleteBlockchain => onDeleteBlockchain()
    case GetBlockFromChain(hash) => onGetBlockFromChain(hash)
    case GetTxOfBlock(id, hash) => onGetTxOfBlock(id, hash)
    case AddBlock(block) => onAddBlock(block)
    case AddBlockFromPool(hash) => onAddBlockFromPool(hash)
    case RemoveBlock => onRemoveBlock()
    case MineNextBlock(data, ids) => onMineNextBlock(data, ids)
    case CheckBlockchainValidity => onCheckBlockchainValidity()
    case GetBlockFromPool(hash) => onGetBlockFromPool(hash)
    case _ => unhandled _
  }

  /**
   * TODO (Chang):
   *
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

  private def onGetBlockFromChain(hash: String): Unit = sender() ! getBlockFromChain(hash)

  private def onGetTxOfBlock(id: String, hash: String): Unit = getBlockFromChain(hash) match {
    case Some(block) => sender() ! block.transactions.find(_.id == id)
    case None => sender() ! None
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

  private def onAddBlockFromPool(hash: String): Unit = blockchainOpt match {
    case Some(blockchain) =>
      val maybeBlock: Future[Option[Block]] = (blocksActor ? BlocksActor.GetBlock(hash)).mapTo[Option[Block]]
      maybeBlock onComplete {
        case Success(blockOpt) => blockOpt match {
          case Some(block) =>
            blockchainOpt = Some(blockchain.addBlock(block))
            hashIndexMapping += (block.hash -> blockchain.length)
            sender() ! SuccessMsg(s"New Block ${block.hash} added on the chain.")
          case None => sender() ! FailureMsg(s"Does not find Block $hash in the poll.")
        }
        case Failure(t) => sender() ! FailureMsg(s"Cannot get Block $hash from the poll.")
      }
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

  private def onMineNextBlock(data: String, ids: Seq[String]): Unit = blockchainOpt match {
    case Some(blockchain) =>
      if (ids.isEmpty) sender() ! Some(blockchain.mineNextBlock(data, Seq.empty[Transaction]))
      else {
        val maybeTrans: Future[Seq[Transaction]] =
          (transActor ? TransactionsActor.GetTransactions(ids.toSet)).mapTo[Seq[Transaction]]
        maybeTrans onComplete {
          case Success(trans) => sender() ! Some(blockchain.mineNextBlock(data, trans))
          case Failure(_) => sender() ! None
        }
      }
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

  private def onGetBlockFromPool(hash: String): Unit = blocksActor forward BlocksActor.GetBlock(hash)


  private def getBlockFromChain(hash: String): Option[Block] = hashIndexMapping.get(hash) match {
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
