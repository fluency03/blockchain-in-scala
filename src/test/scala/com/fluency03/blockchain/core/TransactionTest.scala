package com.fluency03.blockchain.core

import java.time.Instant

import com.fluency03.blockchain.Util.hashOf
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods.parse
import org.scalatest.{FlatSpec, Matchers}

class TransactionTest extends FlatSpec with Matchers {

  "A Transaction" should "be  valid." in {
    val time = Instant.parse("2018-04-11T18:52:01Z").getEpochSecond
    val t = Transaction(ZERO64, ZERO64, 50, time)

    t.sender shouldEqual ZERO64
    t.receiver shouldEqual ZERO64
    t.amount shouldEqual 50
    t.hash shouldEqual hashOf(t.sender, t.receiver, t.amount.toString, time.toString)

    val json = ("sender" -> ZERO64) ~
        ("receiver" -> ZERO64) ~
        ("amount" -> 50.toDouble) ~
        ("timestamp" -> time)
    t.toJson shouldEqual json
    parse(t.toString) shouldEqual json
  }

}
