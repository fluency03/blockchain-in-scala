package com.fluency03.blockchain.api.actors

import akka.actor.{Actor, ActorLogging, Props}
import com.fluency03.blockchain.core.Blockchain

object BlockchainRegistryActor {
  final case class ActionPerformed(description: String)
  final case object GetBlockchain
  final case object CreateBlockchain
  final case object DeleteBlockchain

  def props: Props = Props[BlockchainRegistryActor]
}

class BlockchainRegistryActor extends Actor with ActorLogging {
  import BlockchainRegistryActor._

  var blockchainOpt: Option[Blockchain] = None

  def receive: Receive = {
    case GetBlockchain =>
      sender() ! blockchainOpt.get
    case CreateBlockchain =>
      blockchainOpt = Some(Blockchain())
      sender() ! ActionPerformed(s"Blockchain created, with difficulty ${blockchainOpt.get.difficulty}.")
    case DeleteBlockchain =>
      blockchainOpt = None
      sender() ! ActionPerformed(s"Blockchain deleted.")
  }
}
