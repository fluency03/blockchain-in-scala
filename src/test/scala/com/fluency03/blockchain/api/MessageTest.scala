package com.fluency03.blockchain.api

import org.scalatest.{FlatSpec, Matchers}

class MessageTest extends FlatSpec with Matchers {

  "A Message" should "contain valid content." in {
    Success("Response test.") shouldBe a[Message]
    Success("Response test.").content shouldEqual "Response test."
  }

  "A Input" should "contain valid content." in {
    Input("Input test.").content shouldEqual "Input test."
  }

  "A Fail" should "contain valid content." in {
    Fail("Error") shouldBe a[Message]
    Fail("Error").content shouldEqual "Error"
  }

}
