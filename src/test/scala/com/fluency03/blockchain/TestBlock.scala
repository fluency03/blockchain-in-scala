package com.fluency03.blockchain

import org.scalatest.FlatSpec

class TestBlock extends FlatSpec {

  "Genesis block" should "have index 0" in {
    val genesis: Block = Block.genesis()
    assert(genesis.header.index == 0)
  }


}
