package com.fluency03.blockchain

import java.time.format.DateTimeParseException

import com.fluency03.blockchain.Util._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class UtilTest extends FlatSpec with Matchers with MockFactory {

  "hashOf" should "convert a String to SHA256 hash." in {
    hashOf("open sesame") shouldEqual "41ef4bb0b23661e66301aac36066912dac037827b4ae63a7b1165a5aa93ed4eb"
    hashOf("open", " ", "sesame") shouldEqual "41ef4bb0b23661e66301aac36066912dac037827b4ae63a7b1165a5aa93ed4eb"
    hashOf("") shouldEqual "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
    hashOf("0000000000000000000000000000000000000000000000000000000000000") shouldEqual "a738b0b5c122d30af5b9da1c63c5d590a31aeafa7de1723ee9b5e3a11c9def35"
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
    base64Of("open sesame") shouldEqual "b3BlbiBzZXNhbWU="
    base64Of("open sesame".toCharArray.map(_.toByte)) shouldEqual "b3BlbiBzZXNhbWU="
    fromBase64("b3BlbiBzZXNhbWU=") shouldEqual "open sesame"
    fromBase64(base64Of("aeqfedq.'.[pl12l3[p,5`>}{::>{:")) shouldEqual "aeqfedq.'.[pl12l3[p,5`>}{::>{:"
  }

  "epochTimeOf" should "obtain the Unix epoch of a time." in {
    epochTimeOf("2018-04-11T18:52:01Z") shouldEqual 1523472721
    epochTimeOf("2017-04-11T18:52:01Z") shouldEqual 1491936721
    epochTimeOf("2017-04-02T18:52:01Z") shouldEqual 1491159121
    epochTimeOf("2017-05-02T18:52:01Z") shouldEqual 1493751121
    epochTimeOf("1970-01-01T00:00:00Z") shouldEqual 0
    an[DateTimeParseException] should be thrownBy epochTimeOf("1970-01-01T00:00:00")
  }

  "binaryOfHash" should "convert hex hash to binary." in {
    binaryOfHash("a") shouldEqual "1010"
    binaryOfHash("ab") shouldEqual "10101011"
    an[NumberFormatException] should be thrownBy binaryOfHash("g")
  }

}
