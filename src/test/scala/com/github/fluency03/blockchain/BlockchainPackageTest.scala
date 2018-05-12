package com.github.fluency03.blockchain

import java.time.format.DateTimeParseException

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class BlockchainPackageTest extends FlatSpec with Matchers with MockFactory {

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
    binaryOfHex("a") shouldEqual "1010"
    binaryOfHex("ab") shouldEqual "10101011"
    an[NumberFormatException] should be thrownBy binaryOfHex("g")
  }

  "StringImplicit" should "convert String to corresponding type." in {
    fromBase64("open sesame".toBase64) shouldEqual "open sesame"
    "open sesame".sha256 shouldEqual "41ef4bb0b23661e66301aac36066912dac037827b4ae63a7b1165a5aa93ed4eb"
    "".sha256 shouldEqual "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
    "a".hex2Binary shouldEqual "1010"
    "a".hex2BigInt shouldEqual BigInt(10)
    "ab".hex2Binary shouldEqual "10101011"
    "ab".hex2BigInt shouldEqual BigInt(171)
    "bitcoin".ripemd160 shouldEqual "5891bf40b0b0e8e19f524bdc2e842d012264624b"
    "blockchain".ripemd160 shouldEqual "5c403af45cae136a79eea3c7e9f79c3dd049776b"
  }

  "BytesImplicit" should "convert Array of Byte to corresponding type." in {
    val bytes: Bytes = Array(192.toByte, 168.toByte, 1, 9)
    bytes.toHex shouldEqual "c0a80109"
    bytes.toBase64 shouldEqual "wKgBCQ=="
    "open sesame".getBytes.sha256 shouldEqual "41ef4bb0b23661e66301aac36066912dac037827b4ae63a7b1165a5aa93ed4eb"
    "open sesame".getBytes.sha256Digest shouldEqual ("41ef4bb0b23661e66301aac36066912da" +
      "c037827b4ae63a7b1165a5aa93ed4eb").hex2Bytes

    "173BDED8F2A2069C193E63EA30DC8FD20E815EC3642B9C24AD7002C03D1BFB9B".hex2Bytes.ripemd160 shouldEqual
      "88C2D2FA846282C870A76CADECBE45C4ACD72BB6".toLowerCase

    "173BDED8F2A2069C193E63EA30DC8FD20E815EC3642B9C24AD7002C03D1BFB9B".hex2Bytes.ripemd160ODigest shouldEqual
      "88C2D2FA846282C870A76CADECBE45C4ACD72BB6".hex2Bytes

    ("04b4d653fcbb4b96000c99343f23b08a44fa306031e0587f9e657ab4a25411" +
      "29368d7d9bb05cd8afbdf7705a6540d98028236965553f91bf1c5b4f70073f55b55d").hex2Bytes.hash160Digest shouldEqual
      "88C2D2FA846282C870A76CADECBE45C4ACD72BB6".hex2Bytes

    ("04b4d653fcbb4b96000c99343f23b08a44fa306031e0587f9e657ab4a25411" +
      "29368d7d9bb05cd8afbdf7705a6540d98028236965553f91bf1c5b4f70073f55b55d").hex2Bytes.hash160 shouldEqual
      "88C2D2FA846282C870A76CADECBE45C4ACD72BB6".toLowerCase
  }

  "genesisTimestamp" should "be the Epoch time of 2018-04-11T18:52:01Z ." in {
    genesisTimestamp shouldEqual 1523472721
  }

}
