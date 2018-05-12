package com.github.fluency03.blockchain.api.actors

import akka.actor.{ActorRef, ActorSelection, Props}
import akka.pattern.ask
import com.github.fluency03.blockchain.api.actors.BlockchainActor._
import com.github.fluency03.blockchain.api._
import com.github.fluency03.blockchain.core.{Block, Blockchain, Transaction}

import scala.collection.mutable
import scala.util.{Failure, Success}

object BlockchainActor {
  sealed trait BlockchainMsg
  sealed trait BlockMsg

  final case object GetBlockchain extends BlockchainMsg
  final case object CreateBlockchain extends BlockchainMsg
  final case object DeleteBlockchain extends BlockchainMsg
  final case object CheckBlockchainValidity extends BlockchainMsg

  final case class GetBlockByHash(hash: String) extends BlockMsg
  final case class GetBlocksByHashesAndIndices(hashes: Set[String], indices: Set[Int]) extends BlockMsg
  final case object GetLastBlock extends BlockMsg
  final case class GetTxOfBlock(id: String, hash: String) extends BlockMsg
  final case class AppendBlock(block: Block) extends BlockMsg
  final case class AppendBlockFromPool(hash: String) extends BlockMsg
  final case object RemoveLastBlock extends BlockMsg
  final case class MineNextBlock(data: String, ids: Seq[String]) extends BlockMsg
  final case class GetBlockFromPool(hash: String) extends BlockMsg

  def props: Props = Props[BlockchainActor]
}

class BlockchainActor extends ActorSupport {
  override def preStart(): Unit = log.info("{} started!", this.getClass.getSimpleName)
  override def postStop(): Unit = log.info("{} stopped!", this.getClass.getSimpleName)

  import context.dispatcher

  val blockPoolActor: ActorSelection = context.actorSelection(PARENT_UP + BLOCK_POOL_ACTOR_NAME)
  val txPoolActor: ActorSelection = context.actorSelection(PARENT_UP + TX_POOL_ACTOR_NAME)
  val networkActor: ActorSelection = context.actorSelection(PARENT_UP + NETWORK_ACTOR_NAME)

  // TODO (Chang): need persistence
  var blockchainOpt: Option[Blockchain] = None
  val hashIndexMapping: mutable.Map[String, Int] = mutable.Map.empty[String, Int]

  def receive: Receive = {
    case msg: BlockchainMsg => inCaseOfBlockchainMsg(msg)
    case msg: BlockMsg => inCaseOfBlockMsg(msg)
    case _ => unhandled _
  }

  private def inCaseOfBlockchainMsg(msg: BlockchainMsg): Unit = msg match {
    case GetBlockchain => onGetBlockchain()
    case CreateBlockchain => onCreateBlockchain()
    case DeleteBlockchain => onDeleteBlockchain()
    case CheckBlockchainValidity => onCheckBlockchainValidity()
  }

  private def inCaseOfBlockMsg(msg: BlockMsg): Unit = msg match {
    case GetBlockByHash(hash) => onGetBlockByHash(hash)
    case GetBlocksByHashesAndIndices(hashes, indices) =>
      onGetBlocksByHashesAndIndices(hashes, indices)
    case GetLastBlock => onGetLastBlock()
    case GetTxOfBlock(id, hash) => onGetTxOfBlock(id, hash)
    case AppendBlock(block) => onAppendBlock(block)
    case AppendBlockFromPool(hash) => onAppendBlockFromPool(hash)
    case RemoveLastBlock => onRemoveLastBlock()
    case MineNextBlock(data, ids) => onMineNextBlock(data, ids)
    case GetBlockFromPool(hash) => onGetBlockFromPool(hash)
  }

  /**
   * Handlers for each of the BlockchainMsg.
   */
  private def onGetBlockchain(): Unit = sender() ! blockchainOpt

  private def onCreateBlockchain(): Unit = blockchainOpt match {
    case Some(_) => sender() ! FailureMsg("Blockchain already exists.")
    case None =>
      if (hashIndexMapping.nonEmpty) clearMappingOnNoBlockchain()
      blockchainOpt = Some(Blockchain())
      blockchainOpt.get.chain.zipWithIndex.foreach { case (b, i) =>
        hashIndexMapping += (b.hash -> i)
      }
      sender() ! SuccessMsg(s"Blockchain created, with difficulty ${blockchainOpt.get.difficulty}.")
  }

  private def onDeleteBlockchain(): Unit =  blockchainOpt match {
    case Some(_) =>
      blockchainOpt = None
      hashIndexMapping.clear()
      sender() ! SuccessMsg("Blockchain deleted.")
    case None => sender() ! FailureMsg("Blockchain does not exist.")
  }

  private def onCheckBlockchainValidity(): Unit = blockchainOpt match {
    case Some(blockchain) => sender() ! SuccessMsg(blockchain.isValid.toString)
    case None =>
      clearMappingOnNoBlockchain()
      sender() ! FailureMsg("Blockchain does not exist.")
  }

  /**
   * Handlers for each of the BlockMsg.
   */
  private def onGetBlockByHash(hash: String): Unit = sender() ! getBlockByHash(hash)

  private def onGetBlocksByHashesAndIndices(hashes: Set[String], indices: Set[Int]): Unit =
    sender() ! getBlocksByHashesAndIndices(hashes, indices)

  private def onGetLastBlock(): Unit = blockchainOpt match {
    case Some(blockchain) => sender() ! blockchain.lastBlock()
    case None =>
      clearMappingOnNoBlockchain()
      sender() ! None
  }

  private def onGetTxOfBlock(id: String, hash: String): Unit = getBlockByHash(hash) match {
    case Some(block) => sender() ! block.transactions.find(_.id == id)
    case None => sender() ! None
  }

  private def onAppendBlock(block: Block): Unit = blockchainOpt match {
    case Some(blockchain) =>
      appendBlock(block, blockchain)
      sender() ! SuccessMsg(s"New Block ${block.hash} added on the chain.")
    case None =>
      clearMappingOnNoBlockchain()
      sender() ! FailureMsg("Blockchain does not exist.")
  }

  private def onAppendBlockFromPool(hash: String): Unit = blockchainOpt match {
    case Some(blockchain) =>
      val theSender: ActorRef = sender()
      (blockPoolActor ? BlockPoolActor.GetBlock(hash))
        .mapTo[Option[Block]]
        .onComplete {
          case Success(blockOpt) => blockOpt match {
            case Some(block) =>
              appendBlock(block, blockchain)
              theSender ! SuccessMsg(s"New Block ${block.hash} added on the chain.")
            case None => theSender ! FailureMsg(s"Did not find Block $hash in the poll.")
          }
          case Failure(e) =>
            theSender ! FailureMsg(s"Cannot get Block $hash from the poll: ${e.getMessage}")
        }
    case None =>
      clearMappingOnNoBlockchain()
      sender() ! FailureMsg("Blockchain does not exist.")
  }

  private def onRemoveLastBlock(): Unit = blockchainOpt match {
    case Some(blockchain) =>
      val toBeRemoved = blockchain.chain.head
      blockchainOpt = Some(blockchain.removeBlock())
      hashIndexMapping -= toBeRemoved.hash
      sender() ! SuccessMsg(s"Last Block ${toBeRemoved.hash} removed from the chain.")
    case None =>
      clearMappingOnNoBlockchain()
      sender() ! FailureMsg("Blockchain does not exist.")
  }

  private def onMineNextBlock(data: String, ids: Seq[String]): Unit = blockchainOpt match {
    case Some(blockchain) =>
      if (ids.isEmpty) sender() ! Some(blockchain.mineNextBlock(data, Seq.empty[Transaction]))
      else {
        val theSender: ActorRef = sender()
        (txPoolActor ? TxPoolActor.GetTransactions(ids))
          .mapTo[Seq[Transaction]]
          .onComplete {
            case Success(trans) => theSender ! Some(blockchain.mineNextBlock(data, trans))
            case Failure(_) => theSender ! None
          }
      }
    case None =>
      clearMappingOnNoBlockchain()
      sender() ! None
  }

  private def onGetBlockFromPool(hash: String): Unit =
    blockPoolActor forward BlockPoolActor.GetBlock(hash)

  /**
   * Private helper methods.
   */
  private def getBlockByHash(hash: String): Option[Block] = hashIndexMapping.get(hash) match {
    case Some(index) => blockchainOpt match {
      case Some(blockchain) => Some(blockchain.chain(index))
      case None =>
        clearMappingOnNoBlockchain()
        None
    }
    case None => None
  }

  private def getBlocksByHashesAndIndices(hashes: Set[String], indices: Set[Int]): Set[Block] = {
    ???
  }

  private def appendBlock(block: Block, blockchain: Blockchain): Unit = {
    blockchainOpt = Some(blockchain.addBlock(block))
    hashIndexMapping += (block.hash -> blockchain.length)
  }

  private def clearMappingOnNoBlockchain(): Unit = {
    log.error("Blockchain does not exist! Clear the hash-to-index mapping!")
    hashIndexMapping.clear()
  }

}
