package com.github.fluency03.blockchain
package crypto

import org.scalatest.{FlatSpec, Matchers}

class Base58Test extends FlatSpec with Matchers {

  "Base58" should "encode String to Base58 and decode it back to original." in {
    Base58.encodeString("abc") shouldEqual "ZiCa"
    Base58.encodeString("bitcoin") shouldEqual "4jJc4sAwPs"
    Base58.encodeString("blockchain") shouldEqual "6XidGdGeMggztM"
    Base58.encodeString("00F5F2D624CFB5C3F66D06123D0829D1C9CEBF770E2C13A798") shouldEqual
      "bSMTi3tDLFwyLC26U3SB8ctp7Y4iCcGpXxztHUWQSeo4tXV6p7WABrsxVa4tB7n8e8iT"

    Base58.decodeToString("ZiCa") shouldEqual "abc"
    Base58.decodeToString("4jJc4sAwPs") shouldEqual "bitcoin"
    Base58.decodeToString("6XidGdGeMggztM") shouldEqual "blockchain"

    Base58.encodeString("04b4d653fcbb4b96000c99343f23b08a44fa306031e0587f9e657ab4a254112" +
      "9368d7d9bb05cd8afbdf7705a6540d98028236965553f91bf1c5b4f70073f55b55d") shouldEqual
      "2f6iufmY2PoZhwnZkWwvNYmN6A3G4dH8TSDH1Y5FKpC7yCxoJfqStHLBmkUYrwkekaYUttiAwYWCtioTWJn1s" +
        "mMSGtMwsyLqmSLpQbLrTpSrMNjKcNzL1viBDBkFuJEM3KMtPAx2g2hVLeBFDP79iaqHvwuVyu2zaViPVeLjWtRpWzu4sM"

    Base58.decodeToString("2f6iufmY2PoZhwnZkWwvNYmN6A3G4dH8TSDH1Y5FKpC7yCxoJfqStHLBmkUYrwkekaYUttiAwYWCtioTWJn1s" +
      "mMSGtMwsyLqmSLpQbLrTpSrMNjKcNzL1viBDBkFuJEM3KMtPAx2g2hVLeBFDP79iaqHvwuVyu2zaViPVeLjWtRpWzu4sM") shouldEqual
      "04b4d653fcbb4b96000c99343f23b08a44fa306031e0587f9e657ab4a254112" +
        "9368d7d9bb05cd8afbdf7705a6540d98028236965553f91bf1c5b4f70073f55b55d"

    Base58.encodeHex("0088C2D2FA846282C870A76CADECBE45C4ACD72BB655DA1216") shouldEqual
      "1DU8Hi1sbHTpEP9vViBEkEw6noeUrgKkJH"

    Base58.decodeToHex("1DU8Hi1sbHTpEP9vViBEkEw6noeUrgKkJH") shouldEqual
      "0088C2D2FA846282C870A76CADECBE45C4ACD72BB655DA1216".toLowerCase()

    Base58.encodeHex("00A6020CF86FECEDEEF8859C172BAA1A15A575BC6F27D7CF19") shouldEqual
      "1G8mdKwihfYAxeb2E7XwtE94myognJxibS"

    Base58.decodeToHex("1G8mdKwihfYAxeb2E7XwtE94myognJxibS") shouldEqual
      "00A6020CF86FECEDEEF8859C172BAA1A15A575BC6F27D7CF19".toLowerCase()

    Base58.checkEncode("00A6020CF86FECEDEEF8859C172BAA1A15A575BC6F".hex2Bytes) shouldEqual
      "1G8mdKwihfYAxeb2E7XwtE94myognJxibS"

    Base58.checkDecode(Base58.checkEncode("00A6020CF86FECEDEEF8859C172BAA1A15A575BC6F".hex2Bytes)) shouldEqual
      "00A6020CF86FECEDEEF8859C172BAA1A15A575BC6F".hex2Bytes

    Base58.checkEncode("009568D21EB9B6AAB3F77DBA0650930985EFDE6AE8".hex2Bytes) shouldEqual
      "1Ed1FfBXKH6eDQa3p7LyaxvxJamHH2HvCQ"

    Base58.checkDecode(Base58.checkEncode("009568D21EB9B6AAB3F77DBA0650930985EFDE6AE8".hex2Bytes)) shouldEqual
      "009568D21EB9B6AAB3F77DBA0650930985EFDE6AE8".hex2Bytes

  }

}
