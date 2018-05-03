package com.fluency03.blockchain
package api.actors

import java.security.KeyPair

import akka.actor.Props
import com.fluency03.blockchain.api.actors.PeerActor._
import com.fluency03.blockchain.core.{Peer, SingleWallet}

import scala.collection.mutable

object PeerActor {
  final case object GetPublicKeys
  final case object CreateWallet
  def props: Props = Props[PeerActor]
}

class PeerActor extends ActorSupport {
  override def preStart(): Unit = {
    log.info("{} started!", this.getClass.getSimpleName)
    addWallet()
    log.info("Created initial wallet: {}", wallets.head._1)
  }
  override def postStop(): Unit = log.info("{} stopped!", this.getClass.getSimpleName)

  // TODO (Chang): need persistence
  val wallets = mutable.Map.empty[String, SingleWallet]
  val others = mutable.Map.empty[String, Peer]

  /**
   * TODO (Chang):
   *  - Remove wallet
   *  - Sign transactions
   *
   */

  def receive: Receive = {
    case GetPublicKeys => sender() ! wallets.values.map(_.address).toSet
    case CreateWallet => sender() ! addWallet()
    case _ => unhandled _
  }

  private def addWallet(): String = {
    val newWallet = SingleWallet()
    wallets += (newWallet.address -> newWallet)
    newWallet.address
  }

}
