package com.fluency03.blockchain.api.actors

import akka.actor.{Actor, ActorLogging, ActorSelection, Props}
import com.fluency03.blockchain.api.actors.TransactionActor._
import com.fluency03.blockchain.api.utils.GenericMessage._
import com.fluency03.blockchain.api.{BLOCKCHAIN_ACTOR_NAME, BLOCK_ACTOR_NAME, PARENT_UP}
import com.fluency03.blockchain.core.Transaction

object TransactionActor {
  final case object GetTransactions
  final case class CreateTransaction(tx: Transaction)
  final case class GetTransaction(hash: String)
  final case class DeleteTransaction(hash: String)

  def props: Props = Props[TransactionActor]
}

class TransactionActor extends Actor with ActorLogging {
  override def preStart(): Unit = log.info("{} started!", this.getClass.getSimpleName)
  override def postStop(): Unit = log.info("{} stopped!", this.getClass.getSimpleName)

  val blockchainActor: ActorSelection = context.actorSelection(PARENT_UP + BLOCKCHAIN_ACTOR_NAME)
  val blockActor: ActorSelection = context.actorSelection(PARENT_UP + BLOCK_ACTOR_NAME)

  def receive: Receive = {
    case _ => blockchainActor forward _
  }

//
//  {
//    case msg @ GetTransactions => blockchainActor forward msg
//    case msg: CreateTransaction => blockchainActor forward msg
//    case msg: GetTransaction => blockchainActor forward msg
//    case msg: DeleteTransaction => blockchainActor forward msg
//  }


}
