package com.fluency03.blockchain.api.utils

import com.fluency03.blockchain.api.utils.GenericMessage.Response
import org.scalatest.{FlatSpec, Matchers}

class GenericMessageTest extends FlatSpec with Matchers {

  "A Response" should "contain " in {
    Response("Response test.").message shouldEqual "Response test."
  }

}
