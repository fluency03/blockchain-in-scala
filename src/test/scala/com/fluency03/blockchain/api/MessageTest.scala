package com.fluency03.blockchain.api

import org.scalatest.{FlatSpec, Matchers}

class MessageTest extends FlatSpec with Matchers {

  "A Message" should "contain valid content." in {
    SuccessMsg("Response test.") shouldBe a[Message]
    SuccessMsg("Response test.").message shouldEqual "Response test."
  }

  "A Input" should "contain valid content." in {
    Input("Input test.").content shouldEqual "Input test."
  }

  "A Fail" should "contain valid content." in {
    FailureMsg("Error") shouldBe a[Message]
    FailureMsg("Error").error shouldEqual "Error"
  }

}
