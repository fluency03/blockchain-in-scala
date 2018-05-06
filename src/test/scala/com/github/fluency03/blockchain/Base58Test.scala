package com.github.fluency03.blockchain

import org.scalatest.{FlatSpec, Matchers}

class Base58Test extends FlatSpec with Matchers {

  "Base58" should "encode String to Base58 and decode it back to original." in {
    Base58.encodeString("abc") shouldEqual "ZiCa"
    Base58.encodeString("bitcoin") shouldEqual "4jJc4sAwPs"
    Base58.encodeString("blockchain") shouldEqual "6XidGdGeMggztM"
    Base58.encodeString("00F5F2D624CFB5C3F66D06123D0829D1C9CEBF770E2C13A798") shouldEqual
      "bSMTi3tDLFwyLC26U3SB8ctp7Y4iCcGpXxztHUWQSeo4tXV6p7WABrsxVa4tB7n8e8iT"

    Base58.decodeToHex("ZiCa") shouldEqual "abc"
    Base58.decodeToHex("4jJc4sAwPs") shouldEqual "bitcoin"
    Base58.decodeToHex("6XidGdGeMggztM") shouldEqual "blockchain"

    Base58.encodeString("04b4d653fcbb4b96000c99343f23b08a44fa306031e0587f9e657ab4a254112" +
      "9368d7d9bb05cd8afbdf7705a6540d98028236965553f91bf1c5b4f70073f55b55d") shouldEqual
      "2f6iufmY2PoZhwnZkWwvNYmN6A3G4dH8TSDH1Y5FKpC7yCxoJfqStHLBmkUYrwkekaYUttiAwYWCtioTWJn1s" +
        "mMSGtMwsyLqmSLpQbLrTpSrMNjKcNzL1viBDBkFuJEM3KMtPAx2g2hVLeBFDP79iaqHvwuVyu2zaViPVeLjWtRpWzu4sM"

    Base58.decodeToHex("2f6iufmY2PoZhwnZkWwvNYmN6A3G4dH8TSDH1Y5FKpC7yCxoJfqStHLBmkUYrwkekaYUttiAwYWCtioTWJn1s" +
      "mMSGtMwsyLqmSLpQbLrTpSrMNjKcNzL1viBDBkFuJEM3KMtPAx2g2hVLeBFDP79iaqHvwuVyu2zaViPVeLjWtRpWzu4sM") shouldEqual
      "04b4d653fcbb4b96000c99343f23b08a44fa306031e0587f9e657ab4a254112" +
        "9368d7d9bb05cd8afbdf7705a6540d98028236965553f91bf1c5b4f70073f55b55d"

    ripemd160Of("61956bf4e271df1cd88a9a7828a59c88eb7ea13c176c4d03355ac27129760673") shouldEqual
      "352b0b6bd7284755d5c685fb7793c9f4d672c5ff"
    ripemd160Of("abcd") shouldEqual "2e7e536fd487deaa943fda5522d917bdb9011b7a"
    ripemd160Of("205575f4f33a39ff47f569613a694c6321d6cdd7") shouldEqual "bd4e962413308b4a6689aa0e7cff5e419391c3db"
    ripemd160Of("bitcoin") shouldEqual "5891bf40b0b0e8e19f524bdc2e842d012264624b"
    ripemd160Of("blockchain") shouldEqual "5c403af45cae136a79eea3c7e9f79c3dd049776b"

    Crypto.publicKeyToAddress("04B4D653FCBB4B96000C99343F23B08A44FA306031E0587F9E657AB" +
      "4A2541129368D7D9BB05CD8AFBDF7705A6540D98028236965553F91BF1C5B4F70073F55B55D") shouldEqual
      "1DU8Hi1sbHTpEP9vViBEkEw6noeUrgKkJH"

    Base58.encodeHex("0088C2D2FA846282C870A76CADECBE45C4ACD72BB655DA1216") shouldEqual
      "1DU8Hi1sbHTpEP9vViBEkEw6noeUrgKkJH"

  }

}
