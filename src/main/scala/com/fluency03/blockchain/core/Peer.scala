package com.fluency03.blockchain.core

case class PeerSimple(name: String)
case class Peer(name: String, publicKeys: Set[String])
