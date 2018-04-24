package com.fluency03.blockchain

import akka.util.Timeout
import com.fluency03.blockchain.core.{Block, Transaction}

import scala.concurrent.duration._

package object api {

  type Blocks = Seq[Block]
  type Transactions = Seq[Transaction]

  // Required by the `ask` (?) method
  implicit lazy val timeout: Timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  val BLOCKS_ACTOR_NAME = "blocksActor"
  val BLOCKCHAIN_ACTOR_NAME = "blockchainActor"
  val NETWORK_ACTOR_NAME = "networkActor"
  val PEER_ACTOR_NAME = "peerActor"
  val TRANS_ACTOR_NAME = "transActor"

  val PARENT_UP = "../"

}
