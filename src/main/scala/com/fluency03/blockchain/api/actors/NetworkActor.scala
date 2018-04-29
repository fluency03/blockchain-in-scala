package com.fluency03.blockchain.api.actors

import akka.actor.{ActorSelection, Props}
import akka.pattern.{ask, pipe}
import com.fluency03.blockchain.api._
import com.fluency03.blockchain.api.actors.NetworkActor._
import com.fluency03.blockchain.api.actors.PeerActor.GetPublicKeys
import com.fluency03.blockchain.core.Peer

import scala.concurrent.Future

object NetworkActor {
  final case object GetNetwork
  final case object GetPeers
  final case class GetPeers(names: Set[String])
  final case class CreatePeer(name: String)
  final case class GetPeer(name: String)
  final case class DeletePeer(name: String)

  def props: Props = Props[NetworkActor]
}

class NetworkActor extends ActorSupport {
  override def preStart(): Unit = log.info("{} started!", this.getClass.getSimpleName)
  override def postStop(): Unit = log.info("{} stopped!", this.getClass.getSimpleName)

  import context.dispatcher

  val blockActor: ActorSelection = context.actorSelection(PARENT_UP + BLOCKS_ACTOR_NAME)
  val blockchainActor: ActorSelection = context.actorSelection(PARENT_UP + BLOCKCHAIN_ACTOR_NAME)
  val transActor: ActorSelection = context.actorSelection(PARENT_UP + TRANS_ACTOR_NAME)

  // TODO (Chang): need persistence

  /**
   * TODO (Chang):
   *  - Remove wallet
   *  - Sign transaction
   *
   */

  def receive: Receive = {
    case GetNetwork => onGetNetwork()
    case GetPeers => onGetPeers()
    case GetPeers(names) => onGetPeers(names)
    case CreatePeer(name) => onCreatePeer(name)
    case GetPeer(name) => onGetPeer(name)
    case DeletePeer(name) => onDeletePeer(name)
    case _ => unhandled _
  }

  private def onGetNetwork(): Unit = sender() ! context.children.map(_.path.name).toSet

  private def onGetPeers(): Unit = Future.sequence(context.children.map(p => {
    (p ? GetPublicKeys).mapTo[Set[String]].map(keys => p.path.name -> keys)
  })).map(_.toMap).pipeTo(sender())

  private def onGetPeers(names: Set[String]): Unit = Future.sequence(context.children
    .filter(p => names.contains(p.path.name))
    .map(p => { (p ? GetPublicKeys).mapTo[Set[String]].map(keys => p.path.name -> keys) })
  ).map(_.toMap).pipeTo(sender())

  private def onCreatePeer(name: String): Unit =
    if (context.child(name).isDefined) sender() ! FailureMsg(s"Peer $name has been created.")
    else {
      val _ = context.actorOf(Props[PeerActor], name)
      sender() ! SuccessMsg(s"Peer $name created.")
    }

  private def onGetPeer(name: String): Unit =
    if (context.child(name).isDefined) {
      (context.child(name).get ? GetPublicKeys)
        .mapTo[Set[String]]
        .map(keys => Some(Peer(name, keys)))
        .pipeTo(sender())
    } else sender() ! None

  private def onDeletePeer(name: String): Unit =
    if (context.child(name).isDefined) {
      context stop context.child(name).get
      sender() ! SuccessMsg(s"Peer $name deleted.")
    } else sender() ! FailureMsg(s"Peer $name does not exist.")

}
