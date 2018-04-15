package com.fluency03.blockchain.core

import com.fluency03.blockchain.Util.hashOf
import com.fluency03.blockchain.core.MerkleNode._
import org.scalatest.{FlatSpec, Matchers}

class MerkleNodeTest extends FlatSpec with Matchers {

  "A empty Merkle Tree" should "have Zero 64 as root hash." in {
    computeRootOfHashes(List()) shouldEqual ZERO64
    computeRoot(List()) shouldEqual ZERO64
  }

  "A empty Merkle Tree with one element" should "have root hash as same as the only element hash." in {
    val h = "41ef4bb0b23661e66301aac36066912dac037827b4ae63a7b1165a5aa93ed4eb"
    computeRootOfHashes(List(h)) shouldEqual h

    val t = Transaction(ZERO64, ZERO64, 50)
    computeRoot(List(t)) shouldEqual t.hash
  }

  "A Merkle Tree" should "have a valid root hash." in {
    val h1 = "41ef4bb0b23661e66301aac36066912dac037827b4ae63a7b1165a5aa93ed4eb"
    val h2 = "000031bee3fa033f2d69ae7d0d9f565bf3a235452ccf8a5edffb78cfbcdd7137"
    val h12 = hashOf(h1, h2)
    computeRootOfHashes(List(h1, h2)) shouldEqual h12

    val h3 = "000031beekdjnvj2310i0i0c4i3jomo1m2km10ijodsjco1edffb78cfbcdd7137"
    val h33 = hashOf(h3, h3)
    computeRootOfHashes(List(h1, h2, h3)) shouldEqual hashOf(h12, h33)

    val t1 = Transaction(ZERO64, ZERO64, 50)
    val t2 = Transaction(ZERO64, ZERO64, 20)
    val th12 = hashOf(t1.hash, t2.hash)
    computeRoot(List(t1, t2)) shouldEqual th12

    val t3 = Transaction(ZERO64, ZERO64, 10)
    val th33 = hashOf(t3.hash, t3.hash)
    computeRoot(List(t1, t2, t3)) shouldEqual hashOf(th12, th33)
  }

}
