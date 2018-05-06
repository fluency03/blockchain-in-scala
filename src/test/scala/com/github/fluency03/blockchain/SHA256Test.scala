package com.github.fluency03.blockchain

import org.scalatest.{FlatSpec, Matchers}

class SHA256Test extends FlatSpec with Matchers {

  "SHA256" should "encode String to SHA256 and decode it back to original." in {
    SHA256.hash("open sesame") shouldEqual "41ef4bb0b23661e66301aac36066912dac037827b4ae63a7b1165a5aa93ed4eb"
    SHA256.hashAll("open", " ", "sesame") shouldEqual "41ef4bb0b23661e66301aac36066912dac037827b4ae63a7b1165a5aa93ed4eb"
    SHA256.hash("") shouldEqual "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
    SHA256.hash("0000000000000000000000000000000000000000000000000000000000000") shouldEqual "a738b0b5c122d30af5b9da1c63c5d590a31aeafa7de1723ee9b5e3a11c9def35"

    SHA256.hash(("04b4d653fcbb4b96000c99343f23b08a44fa306031e0587f9e657ab4a25411" +
      "29368d7d9bb05cd8afbdf7705a6540d98028236965553f91bf1c5b4f70073f55b55d").hex2Bytes) shouldEqual
      "173BDED8F2A2069C193E63EA30DC8FD20E815EC3642B9C24AD7002C03D1BFB9B".toLowerCase

    SHA256.hashToDigest(("04b4d653fcbb4b96000c99343f23b08a44fa306031e0587f9e657ab4a25411" +
      "29368d7d9bb05cd8afbdf7705a6540d98028236965553f91bf1c5b4f70073f55b55d").hex2Bytes) shouldEqual
      "173BDED8F2A2069C193E63EA30DC8FD20E815EC3642B9C24AD7002C03D1BFB9B".hex2Bytes

    SHA256.hash("0088C2D2FA846282C870A76CADECBE45C4ACD72BB6".hex2Bytes) shouldEqual
      "1F87490FC565C795595563D56412A0100CD1F29FFB60A3779789FE0C018C6164".toLowerCase


    SHA256.hash("1F87490FC565C795595563D56412A0100CD1F29FFB60A3779789FE0C018C6164".hex2Bytes) shouldEqual
      "55DA1216A5EF5BAE605B543A5A9CE2AC8A8FA1781AA037F35DE3F2222BAD8127".toLowerCase

    SHA256.hashToDigest("00".hex2Bytes ++ "88C2D2FA846282C870A76CADECBE45C4ACD72BB6".hex2Bytes) shouldEqual
      "1F87490FC565C795595563D56412A0100CD1F29FFB60A3779789FE0C018C6164".hex2Bytes
  }

}
