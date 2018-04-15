package com.fluency03.blockchain

import com.fluency03.blockchain.core.{Block, Transaction}

package object api {

  type Blocks = List[Block]
  type Transactions = List[Transaction]

  lazy val BLOCK_ACTOR_NAME = "blockActor"
  lazy val BLOCKCHAIN_ACTOR_NAME = "blockchainActor"
  lazy val TX_ACTOR_NAME = "txActor"

  lazy val PARENT_UP = "../"

}
