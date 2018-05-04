package com.fluency03.blockchain

import com.fluency03.blockchain.core.{Block, Peer, Transaction}

package object api {

  type Blocks = Seq[Block]
  type Transactions = Seq[Transaction]
  type Peers = Seq[Peer]

  val BLOCK_POOL_ACTOR_NAME = "blocksPoolActor"
  val BLOCKCHAIN_ACTOR_NAME = "blockchainActor"
  val NETWORK_ACTOR_NAME = "networkActor"
  val PEER_ACTOR_NAME = "peerActor"
  val TX_POOL_ACTOR_NAME = "txPoolActor"

  val PARENT_UP = "../"






  

}
