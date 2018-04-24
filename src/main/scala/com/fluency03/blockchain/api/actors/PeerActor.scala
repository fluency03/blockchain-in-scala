package com.fluency03.blockchain
package api.actors

import java.security.{KeyPair, PublicKey}

import akka.actor.{Actor, ActorLogging, Props}
import com.fluency03.blockchain.api.actors.PeerActor._

import scala.collection.mutable

object PeerActor {
  final case object GetPublicKeys
  def props: Props = Props[PeerActor]
}

class PeerActor extends Actors {
  override def preStart(): Unit = log.info("{} started!", this.getClass.getSimpleName)
  override def postStop(): Unit = log.info("{} stopped!", this.getClass.getSimpleName)

  // TODO (Chang): not persistent
  val wallets = mutable.Map.empty[String, KeyPair]
  wallets += {
    val pair: KeyPair = Crypto.generateKeyPair()
    (pair.getPublic.getEncoded.toHex, pair)
  }
  val peerPublicKeys = mutable.Map.empty[String, PublicKey]

  def receive: Receive = {
    case GetPublicKeys => sender() ! wallets.values.map(_.getPublic.getEncoded.toHex).toSeq
    case _ => unhandled _
  }


}
