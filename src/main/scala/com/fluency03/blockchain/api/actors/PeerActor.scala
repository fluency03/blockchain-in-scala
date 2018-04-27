package com.fluency03.blockchain
package api.actors

import java.security.KeyPair

import akka.actor.Props
import com.fluency03.blockchain.api.actors.PeerActor._
import com.fluency03.blockchain.core.Peer

import scala.collection.mutable

object PeerActor {
  final case object GetPublicKeys
  final case object CreateKeyPair
  def props: Props = Props[PeerActor]
}

class PeerActor extends ActorSupport {
  override def preStart(): Unit = log.info("{} started!", this.getClass.getSimpleName)
  override def postStop(): Unit = log.info("{} stopped!", this.getClass.getSimpleName)

  // TODO (Chang): need persistence
  val wallets = mutable.Map.empty[String, KeyPair]
  addNewKeyPair()

  val others = mutable.Map.empty[String, Peer]

  def receive: Receive = {
    case GetPublicKeys => sender() ! wallets.values.map(_.getPublic.toHex).toSet
    case CreateKeyPair => sender() ! addNewKeyPair().getPublic.toHex
    case _ => unhandled _
  }

  private def addNewKeyPair(): KeyPair = {
    val pair: KeyPair = Crypto.generateKeyPair()
    wallets += (pair.getPublic.toHex -> pair)
    pair
  }

}
