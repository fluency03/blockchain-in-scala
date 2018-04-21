package com.fluency03.blockchain.api.actors

import java.security.{KeyPair, PrivateKey, PublicKey}

import akka.actor.{Actor, ActorLogging, Props}

import scala.collection.mutable

object PeerActor {


  def props: Props = Props[PeerActor]
}

class PeerActor extends Actor with ActorLogging {
  override def preStart(): Unit = log.info("{} started!", this.getClass.getSimpleName)
  override def postStop(): Unit = log.info("{} stopped!", this.getClass.getSimpleName)

  // TODO (Chang): not persistent
  val wallets = mutable.Map.empty[String, KeyPair]
  val publicKeys = mutable.Map.empty[String, PublicKey]

  def receive: Receive = {
    case _ => ???
//    case _ => unhandled _
  }



}
