package com.fluency03.blockchain

import org.json4s.JObject
import org.json4s.JsonDSL._

case class Transaction(sender: String, receiver: String, amount: Double) {



  def toJson: JObject =
    ("sender" -> sender) ~
      ("receiver" -> receiver) ~
      ("amount" -> amount)

}



object Transaction {




}
