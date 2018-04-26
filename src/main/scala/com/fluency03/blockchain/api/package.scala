package com.fluency03.blockchain

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import com.fluency03.blockchain.core.{Block, Peer, Transaction}

package object api {

  type Blocks = Seq[Block]
  type Transactions = Seq[Transaction]
  type Peers = Seq[Peer]

  val BLOCKS_ACTOR_NAME = "blocksActor"
  val BLOCKCHAIN_ACTOR_NAME = "blockchainActor"
  val NETWORK_ACTOR_NAME = "networkActor"
  val PEER_ACTOR_NAME = "peerActor"
  val TRANS_ACTOR_NAME = "transActor"

  val PARENT_UP = "../"


}
