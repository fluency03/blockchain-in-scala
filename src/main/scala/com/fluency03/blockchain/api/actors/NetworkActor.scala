package com.fluency03.blockchain.api.actors

import akka.pattern.{ask, pipe}
import akka.actor.{Actor, ActorLogging, ActorSelection, Props}
import com.fluency03.blockchain.api.actors.NetworkActor._
import com.fluency03.blockchain.api.actors.PeerActor.GetPublicKeys
import com.fluency03.blockchain.api.utils.GenericMessage.Response
import com.fluency03.blockchain.api.{BLOCKCHAIN_ACTOR_NAME, BLOCKS_ACTOR_NAME, PARENT_UP, TRANS_ACTOR_NAME}

import scala.concurrent.Future

object NetworkActor {
  final case object GetPeers
  final case class CreatePeer(id: String)
  final case class GetPeer(id: String)
  final case class DeletePeer(id: String)

  def props: Props = Props[NetworkActor]
}

class NetworkActor extends Actor with ActorLogging {
  import context.dispatcher

  override def preStart(): Unit = log.info("{} started!", this.getClass.getSimpleName)
  override def postStop(): Unit = log.info("{} stopped!", this.getClass.getSimpleName)

  val blockActor: ActorSelection = context.actorSelection(PARENT_UP + BLOCKS_ACTOR_NAME)
  val blockchainActor: ActorSelection = context.actorSelection(PARENT_UP + BLOCKCHAIN_ACTOR_NAME)
  val txActor: ActorSelection = context.actorSelection(PARENT_UP + TRANS_ACTOR_NAME)

  // TODO (Chang): children actors are not persistent

  def receive: Receive = {
    case GetPeers => context.children.map(_.path.name).toList
    case CreatePeer(id) =>
      if (context.child(id).isDefined) sender() ! Response(s"Peer $id has been created.")
      else {
        val _ = context.actorOf(Props[PeerActor], name = id)
        sender() ! Response(s"Peer $id created.")
      }
    case GetPeer(id) =>
      if (context.child(id).isDefined) (context.child(id).get ? GetPublicKeys).mapTo[Seq[String]] pipeTo sender()
      else sender() ! None
    case DeletePeer(id) =>
      if (context.child(id).isDefined) {
        context stop context.child(id).get
        sender() ! Response(s"Peer $id deleted.")
      } else sender() ! Response(s"Peer $id does not exist.")
    case _ => unhandled _
  }

}
