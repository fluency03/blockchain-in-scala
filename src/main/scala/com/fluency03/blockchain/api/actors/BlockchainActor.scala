package com.fluency03.blockchain.api.actors

import akka.actor.{ActorSelection, Props}
import com.fluency03.blockchain.api.actors.BlockchainActor._
import com.fluency03.blockchain.api._
import com.fluency03.blockchain.core.{Block, Blockchain}

import scala.collection.mutable

object BlockchainActor {
  final case object GetBlockchain
  final case object CreateBlockchain
  final case object DeleteBlockchain
  final case class GetBlock(hash: String)

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
  var hashIndexMapping = mutable.Map.empty[String, Int]

  def receive: Receive = {
    case GetBlockchain => onGetBlockchain()
    case CreateBlockchain => onCreateBlockchain()
    case DeleteBlockchain => onDeleteBlockchain()
    case GetBlock(hash) => onGetBlock(hash)
    case _ => unhandled _
  }

  /**
   * TODO (Chang): new APIS:
   *  - AddBlockOnBlockchain
   *  - GetBlockFromBlockchain
   *  - CheckBlockchainIsValid
   *  - GetTransactionOfABlock
   *  - MineNextBlock
   *
   */

  private def onGetBlockchain(): Unit = sender() ! blockchainOpt

  private def onCreateBlockchain(): Unit =
    if (blockchainOpt.isDefined) sender() ! FailureMsg(s"Blockchain already exists.")
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
      sender() ! SuccessMsg(s"Blockchain deleted.")
    } else sender() ! FailureMsg(s"Blockchain does not exist.")

  private def onGetBlock(hash: String): Unit = sender() ! {
    hashIndexMapping.get(hash) match {
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


}
