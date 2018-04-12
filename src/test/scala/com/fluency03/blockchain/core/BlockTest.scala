package com.fluency03.blockchain.core

import org.scalatest.FlatSpec

class BlockTest extends FlatSpec {

  "Genesis block" should "have index 0" in {
    val genesis: Block = Block.genesisBlock
    println(genesis)
    assert(genesis.header.index == 0)
  }


}
