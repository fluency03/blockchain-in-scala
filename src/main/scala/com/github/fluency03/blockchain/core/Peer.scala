package com.github.fluency03.blockchain
package core

case class PeerSimple(name: String)
case class Peer(name: String, publicKeys: Set[HexString])
