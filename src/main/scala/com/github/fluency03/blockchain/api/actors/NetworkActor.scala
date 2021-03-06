package com.github.fluency03.blockchain
package api.actors

import akka.actor.{ActorSelection, Props}
import akka.pattern.{ask, pipe}
import com.github.fluency03.blockchain.api._
import com.github.fluency03.blockchain.api.actors.NetworkActor._
import com.github.fluency03.blockchain.api.actors.PeerActor.GetPublicKeys
import com.github.fluency03.blockchain.core.Peer

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

  val blockPoolActor: ActorSelection = context.actorSelection(PARENT_UP + BLOCK_POOL_ACTOR_NAME)
  val blockchainActor: ActorSelection = context.actorSelection(PARENT_UP + BLOCKCHAIN_ACTOR_NAME)
  val txPoolActor: ActorSelection = context.actorSelection(PARENT_UP + TX_POOL_ACTOR_NAME)

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

  /**
   * Handlers for each of the Messages.
   */
  private def onGetNetwork(): Unit = sender() ! context.children.map(_.path.name).toSet

  private def onGetPeers(): Unit = {
    val peers = context.children.map { p =>
      (p ? GetPublicKeys).mapTo[Set[HexString]].map(p.path.name -> _)
    }
    Future.sequence(peers).map(_.toMap).pipeTo(sender())
  }

  private def onGetPeers(names: Set[String]): Unit = {
    val peers = context.children
      .filter { p => names.contains(p.path.name) }
      .map { p => (p ? GetPublicKeys).mapTo[Set[HexString]].map(p.path.name -> _) }
    Future.sequence(peers).map(_.toMap).pipeTo(sender())
  }

  private def onCreatePeer(name: String): Unit = context.child(name) match {
    case Some(_) => sender() ! FailureMsg(s"Peer $name has been created.")
    case None =>
      val _ = context.actorOf(Props[PeerActor], name)
      sender() ! SuccessMsg(s"Peer $name created.")
  }

  private def onGetPeer(name: String): Unit = context.child(name) match {
    case Some(_) => (context.child(name).get ? GetPublicKeys)
      .mapTo[Set[HexString]]
      .map { keys => Some(Peer(name, keys)) }
      .pipeTo(sender())
    case None => sender() ! None
  }

  private def onDeletePeer(name: String): Unit = context.child(name) match {
    case Some(_) =>
      context stop context.child(name).get
      sender() ! SuccessMsg(s"Peer $name deleted.")
    case None => sender() ! FailureMsg(s"Peer $name does not exist.")
  }

}
