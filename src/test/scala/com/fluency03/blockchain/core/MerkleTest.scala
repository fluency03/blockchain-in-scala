package com.fluency03.blockchain
package core

import com.fluency03.blockchain.Util.sha256Of
import com.fluency03.blockchain.core.Merkle._
import com.fluency03.blockchain.core.Transaction.createCoinbaseTx
import org.scalatest.{FlatSpec, Matchers}

class MerkleTest extends FlatSpec with Matchers {

  "A empty Merkle Tree" should "have Zero 64 as root hash." in {
    computeRootOfHashes(List()) shouldEqual ZERO64
    computeRoot(List()) shouldEqual ZERO64
  }

  "A empty Merkle Tree with one element" should "have root hash as same as the only element hash." in {
    val h = "41ef4bb0b23661e66301aac36066912dac037827b4ae63a7b1165a5aa93ed4eb"
    computeRootOfHashes(List(h)) shouldEqual h

    val t: Transaction = createCoinbaseTx(0, genesisMiner, genesisTimestamp)
    computeRoot(List(t)) shouldEqual t.id
  }

  "A Merkle Tree" should "have a valid root hash." in {
    val h1 = "41ef4bb0b23661e66301aac36066912dac037827b4ae63a7b1165a5aa93ed4eb"
    val h2 = "000031bee3fa033f2d69ae7d0d9f565bf3a235452ccf8a5edffb78cfbcdd7137"
    val h12 = sha256Of(h1, h2)
    computeRootOfHashes(List(h1, h2)) shouldEqual h12

    val h3 = "000031beekdjnvj2310i0i0c4i3jomo1m2km10ijodsjco1edffb78cfbcdd7137"
    val h33 = sha256Of(h3, h3)
    computeRootOfHashes(List(h1, h2, h3)) shouldEqual sha256Of(h12, h33)

    val t1 = createCoinbaseTx(1, genesisMiner, genesisTimestamp)
    val t2 = createCoinbaseTx(2, genesisMiner, genesisTimestamp)
    val th12 = sha256Of(t1.id, t2.id)
    computeRoot(List(t1, t2)) shouldEqual th12

    val t3 = createCoinbaseTx(3, genesisMiner, genesisTimestamp)
    val th33 = sha256Of(t3.id, t3.id)
    computeRoot(List(t1, t2, t3)) shouldEqual sha256Of(th12, th33)
  }

}
