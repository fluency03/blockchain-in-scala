package com.github.fluency03.blockchain

import org.scalatest.{FlatSpec, Matchers}

class RIPEMD160Test extends FlatSpec with Matchers {

  "RIPEMD160" should "encode String to RIPEMD160 and decode it back to original." in {

    RIPEMD160.hash("173BDED8F2A2069C193E63EA30DC8FD20E815EC3642B9C24AD7002C03D1BFB9B".hex2Bytes) shouldEqual
      "88C2D2FA846282C870A76CADECBE45C4ACD72BB6".toLowerCase

    RIPEMD160.doubleHashToDigest(("04b4d653fcbb4b96000c99343f23b08a44fa306031e0587f9e657ab4a25411" +
      "29368d7d9bb05cd8afbdf7705a6540d98028236965553f91bf1c5b4f70073f55b55d").hex2Bytes) shouldEqual
      "88C2D2FA846282C870A76CADECBE45C4ACD72BB6".hex2Bytes

    RIPEMD160.doubleHash(("04b4d653fcbb4b96000c99343f23b08a44fa306031e0587f9e657ab4a25411" +
      "29368d7d9bb05cd8afbdf7705a6540d98028236965553f91bf1c5b4f70073f55b55d").hex2Bytes) shouldEqual
      "88C2D2FA846282C870A76CADECBE45C4ACD72BB6".toLowerCase

    RIPEMD160.hash("61956bf4e271df1cd88a9a7828a59c88eb7ea13c176c4d03355ac27129760673") shouldEqual
      "352b0b6bd7284755d5c685fb7793c9f4d672c5ff"
    RIPEMD160.hash("abcd") shouldEqual "2e7e536fd487deaa943fda5522d917bdb9011b7a"
    RIPEMD160.hash("205575f4f33a39ff47f569613a694c6321d6cdd7") shouldEqual "bd4e962413308b4a6689aa0e7cff5e419391c3db"
    RIPEMD160.hash("bitcoin") shouldEqual "5891bf40b0b0e8e19f524bdc2e842d012264624b"
    RIPEMD160.hash("blockchain") shouldEqual "5c403af45cae136a79eea3c7e9f79c3dd049776b"

  }

}
