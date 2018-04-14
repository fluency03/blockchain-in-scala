package com.fluency03.blockchain.api.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.fluency03.blockchain.core.Block

object BlockRegistryActor {
  final case class ActionPerformed(description: String)
  final case object GetBlocks
  final case class CreateBlock(block: Block)
  final case class GetBlock(hash: String)
  final case class DeleteBlock(hash: String)

  def props: Props = Props[TransactionRegistryActor]
}

class BlockRegistryActor extends Actor with ActorLogging {
  import BlockRegistryActor._

  val blockchainActor: ActorRef = context.actorOf(BlockchainRegistryActor.props)

  var blocks = Set.empty[Block]

  def receive: Receive = {
    case GetBlocks => sender() ! blocks.toList
    case CreateBlock(block) =>
      blocks += block
      sender() ! ActionPerformed(s"Block ${block.hash} created.")
    case GetBlock(hash) => sender() ! blocks.find(_.hash == hash)
    case DeleteBlock(hash) =>
      blocks.find(_.hash == hash) foreach { block => blocks -= block }
      sender() ! ActionPerformed(s"Block $hash deleted.")
  }
}
