package com.fluency03.blockchain.api.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.fluency03.blockchain.core.{Block, Transaction}

object TransactionRegistryActor {
  final case class ActionPerformed(description: String)
  final case object GetTransactions
  final case class CreateTransaction(tx: Transaction)
  final case class GetTransaction(hash: String)
  final case class DeleteTransaction(hash: String)

  def props: Props = Props[TransactionRegistryActor]
}

class TransactionRegistryActor extends Actor with ActorLogging {
  import TransactionRegistryActor._

  var transactions = Set.empty[Transaction]

  def receive: Receive = {
    case GetTransactions => sender() ! transactions.toList
    case CreateTransaction(tx) =>
      transactions += tx
      sender() ! ActionPerformed(s"Transaction ${tx.hash} created.")
    case GetTransaction(hash) => sender() ! transactions.find(_.hash == hash)
    case DeleteTransaction(hash) =>
      transactions.find(_.hash == hash) foreach { tx => transactions -= tx }
      sender() ! ActionPerformed(s"Transaction $hash deleted.")
  }
}
