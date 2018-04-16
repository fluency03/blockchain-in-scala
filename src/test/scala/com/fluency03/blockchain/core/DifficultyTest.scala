package com.fluency03.blockchain.core

import com.fluency03.blockchain.core.Difficulty._
import org.scalatest.{FlatSpec, Matchers}

class DifficultyTest extends FlatSpec with Matchers {

  val (t1, n1, o1) = decodeCompact("1d00ffff".hex)
  val (t2, n2, o2) = decodeCompact("453062093".toLong)
  val (t3, n3, o3) = decodeCompact("-453062093".toLong)

  "Bits in different formats" should "have its corresponding targets." in {
    difficultyOneTarget shouldEqual t1
    t1.toString(16) shouldEqual "ffff0000000000000000000000000000000000000000000000000000"
    t2.toString(16) shouldEqual "12dcd000000000000000000000000000000000000000000000000"
    targetOfBits("01003456".hex) shouldEqual "00".hex
    targetOfBits("01123456".hex) shouldEqual "12".hex
    targetOfBits("02008000".hex) shouldEqual "80".hex
    targetOfBits("05009234".hex) shouldEqual "92340000".hex
    targetOfBits("04923456".hex) shouldEqual - "12345600".hex
    targetOfBits("04123456".hex) shouldEqual "12345600".hex
//    println("1d00ffff".hex)
//    println(bitsOfTarget(t1, n1))
//    println(n1, o1)
//    println(bitsOfTarget(t2, n2))
//    println(n2, o2)
//    println(bitsOfTarget(t3, n3))
//    println(n3, o3)
  }

  "padHexTarget" should "add enough zeros (0) in front of a hex fot making it 64 bytes." in {
    padHexTarget(t1.toString(16)) shouldEqual "00000000ffff0000000000000000000000000000000000000000000000000000"
    padHexTarget(t2.toString(16)) shouldEqual "0000000000012dcd000000000000000000000000000000000000000000000000"
    difficultyOf(t1, n1, o1) shouldEqual 1.0
    difficultyOf(t2, n2, o2) shouldEqual 55589.518126868665
//    targetOfBits(bitsOfTarget(t1, n1)) shouldEqual t1
//    targetOfBits(bitsOfTarget(t2, n2)) shouldEqual t2
  }



}
