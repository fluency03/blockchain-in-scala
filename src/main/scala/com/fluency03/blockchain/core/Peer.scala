package com.fluency03.blockchain.core

trait PeerSimple {
  def name: String
}

case class Peer(name: String, publicKeys: Set[String]) extends PeerSimple
