package com.fluency03.blockchain.core

import com.fluency03.blockchain.Util.hashOf
import org.json4s.JsonDSL._
import org.scalatest.{FlatSpec, Matchers}

class TransactionTest extends FlatSpec with Matchers {

  "A Transaction" should "be  valid." in {
    val t = Transaction(ZERO64, ZERO64, 50)

    t.sender shouldEqual ZERO64
    t.receiver shouldEqual ZERO64
    t.amount shouldEqual 50
    t.hash shouldEqual hashOf(t.sender, t.receiver, t.amount.toString)

    val json = ("sender" -> ZERO64) ~ ("receiver" -> ZERO64) ~ ("amount" -> 50.toDouble)
    t.toJson shouldEqual json
    t.toString shouldEqual "{\"sender\":\"" + ZERO64 + "\",\"receiver\":\"" + ZERO64 + "\",\"amount\":50.0}"
  }

}
