package com.fluency03.blockchain.api.actors

import scala.util.{Failure, Success}
import akka.actor.{ActorSelection, Props}
import akka.pattern.ask
import com.fluency03.blockchain.api._
import com.fluency03.blockchain.api.actors.NetworkActor._
import com.fluency03.blockchain.api.actors.PeerActor.GetPublicKeys
import com.fluency03.blockchain.core.Peer

object NetworkActor {
  final case object GetPeers
  final case class CreatePeer(name: String)
  final case class GetPeer(name: String)
  final case class DeletePeer(name: String)

  def props: Props = Props[NetworkActor]
}

class NetworkActor extends Actors {
  override def preStart(): Unit = log.info("{} started!", this.getClass.getSimpleName)
  override def postStop(): Unit = log.info("{} stopped!", this.getClass.getSimpleName)

  import context.dispatcher

  val blockActor: ActorSelection = context.actorSelection(PARENT_UP + BLOCKS_ACTOR_NAME)
  val blockchainActor: ActorSelection = context.actorSelection(PARENT_UP + BLOCKCHAIN_ACTOR_NAME)
  val transActor: ActorSelection = context.actorSelection(PARENT_UP + TRANS_ACTOR_NAME)

  // TODO (Chang): need persistence

  def receive: Receive = {
    case GetPeers => onGetPeers()
    case CreatePeer(name) => onCreatePeer(name)
    case GetPeer(name) => onGetPeer(name)
    case DeletePeer(name) => onDeletePeer(name)
    case _ => unhandled _
  }

  private def onGetPeers(): Unit = context.children.map(_.path.name).toSeq

  private def onCreatePeer(name: String): Unit =
    if (context.child(name).isDefined) sender() ! Fail(s"Peer $name has been created.")
    else {
      val _ = context.actorOf(Props[PeerActor], name)
      sender() ! Success(s"Peer $name created.")
    }

  private def onGetPeer(name: String): Unit =
    if (context.child(name).isDefined) {
      (context.child(name).get ? GetPublicKeys).mapTo[Set[String]] onComplete {
        case Success(result) => sender() ! Some(Peer(name, result))
        case Failure(_) => sender() ! Some(Peer(name, Set()))
      }
    } else sender() ! None

  private def onDeletePeer(name: String): Unit =
    if (context.child(name).isDefined) {
      context stop context.child(name).get
      sender() ! Success(s"Peer $name deleted.")
    } else sender() ! Fail(s"Peer $name does not exist.")

  // TODO (Chang): APIs for selecting Peers based on Seq of ids



}
