package com.fluency03.blockchain

import java.time.Instant

import com.fluency03.blockchain.Util._
import org.scalatest.{FlatSpec, Matchers}
import org.scalamock.scalatest.MockFactory

class UtilTest extends FlatSpec with Matchers with MockFactory {

  "hashOf" should "convert a String to SHA256 hash." in {
    hashOf("open sesame") shouldEqual "41ef4bb0b23661e66301aac36066912dac037827b4ae63a7b1165a5aa93ed4eb"
    hashOf("open", " ", "sesame") shouldEqual "41ef4bb0b23661e66301aac36066912dac037827b4ae63a7b1165a5aa93ed4eb"
  }

  "getCurrentTimestamp" should "be able to get current Unix epoch time." in {
    val t1 = getCurrentTimestamp
    val t2 = getCurrentTimestamp
    println("This test could potentially fail. t2: " + t1 + "; t2: " + t2)
    t1 shouldEqual t2
  }

  "isWithValidDifficulty" should "be able to to check the validity of a hash." in {
    isWithValidDifficulty("0000", 4) shouldEqual true
    isWithValidDifficulty("0000adqwcwv", 4) shouldEqual true
    isWithValidDifficulty("00000adqwcwv", 4) shouldEqual true
    isWithValidDifficulty("000000adqwcwv", 4) shouldEqual true

    isWithValidDifficulty("000", 4) shouldEqual false
    isWithValidDifficulty("000asfqwdq", 4) shouldEqual false
    isWithValidDifficulty("00sadqwdqw", 4) shouldEqual false
    isWithValidDifficulty("qdvfv", 4) shouldEqual false
    isWithValidDifficulty("", 4) shouldEqual false
  }

  "A String" should "be converted to Base64 and converted back." in {
    toBase64("open sesame") shouldEqual "b3BlbiBzZXNhbWU="
    fromBase64("b3BlbiBzZXNhbWU=") shouldEqual "open sesame"
    fromBase64(toBase64("aeqfedq.'.[pl12l3[p,5`>}{::>{:")) shouldEqual "aeqfedq.'.[pl12l3[p,5`>}{::>{:"
  }

}
