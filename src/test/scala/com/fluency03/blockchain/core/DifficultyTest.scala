package com.fluency03.blockchain
package core

import com.fluency03.blockchain.core.Difficulty._
import org.scalatest.{FlatSpec, Matchers}

class DifficultyTest extends FlatSpec with Matchers {

  val (t1, n1, o1) = decodeCompact("1d00ffff".hex2Long)
  val (t2, n2, o2) = decodeCompact("453062093".toLong)
  val (t3, n3, o3) = decodeCompact("-453062093".toLong)

  "Bits in different formats" should "have its corresponding targets." in {
    difficultyOneTarget shouldEqual t1
    t1.toString(16) shouldEqual "ffff0000000000000000000000000000000000000000000000000000"
    t2.toString(16) shouldEqual "12dcd000000000000000000000000000000000000000000000000"
    targetOfBits("01003456".hex2Long) shouldEqual "00".hex2Long
    targetOfBits("01123456".hex2Long) shouldEqual "12".hex2Long
    targetOfBits("02008000".hex2Long) shouldEqual "80".hex2Long
    targetOfBits("05009234".hex2Long) shouldEqual "92340000".hex2Long
    targetOfBits("04923456".hex2Long) shouldEqual - "12345600".hex2Long
    targetOfBits("04123456".hex2Long) shouldEqual "12345600".hex2Long
  }

  "padHexTarget" should "add enough zeros (0) in front of a hex fot making it 64 bytes." in {
    padHexTarget(t1.toString(16)) shouldEqual "00000000ffff0000000000000000000000000000000000000000000000000000"
    padHexTarget(t2.toString(16)) shouldEqual "0000000000012dcd000000000000000000000000000000000000000000000000"
    difficultyOf(t1, n1, o1) shouldEqual 1.0
    difficultyOf(t2, n2, o2) shouldEqual 55589.518126868665
    difficultyOf(0, n2, o2) shouldEqual 0
    difficultyOf(t1, true, o1) shouldEqual 0
    difficultyOf(t1, n1, true) shouldEqual 0
  }

  "getLowBits" should "obtain the N lower bits of a BigInt." in {
    getLowBits(BigInt(1000), 2) shouldEqual 0
    getLowBits(BigInt(1001), 2) shouldEqual 1
    getLowBits(BigInt(1002), 2) shouldEqual 2
    getLowBits(BigInt(1003), 2) shouldEqual 3
    getLowBits(BigInt(1004), 2) shouldEqual 0
    getLowBits(BigInt(1005), 2) shouldEqual 1

    getLowBits(BigInt(1000), 4) shouldEqual 8
    getLowBits(BigInt(1001), 4) shouldEqual 9
    getLowBits(BigInt(1002), 4) shouldEqual 10
    getLowBits(BigInt(1003), 4) shouldEqual 11
    getLowBits(BigInt(1004), 4) shouldEqual 12
    getLowBits(BigInt(1005), 4) shouldEqual 13

    getLowBits(BigInt(1000), 8) shouldEqual 232
  }

}
