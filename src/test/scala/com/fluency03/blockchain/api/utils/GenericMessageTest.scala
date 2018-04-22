package com.fluency03.blockchain.api.utils

import com.fluency03.blockchain.api.utils.GenericMessage.{Input, Response}
import org.scalatest.{FlatSpec, Matchers}

class GenericMessageTest extends FlatSpec with Matchers {

  "A Response" should "contain valid message." in {
    Response("Response test.").message shouldEqual "Response test."
  }

  "A Input" should "contain valid data." in {
    Input("Input test.").data shouldEqual "Input test."
  }

}
