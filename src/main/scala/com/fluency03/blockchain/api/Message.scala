package com.fluency03.blockchain.api

final case class Input(content: String)

sealed trait Message
final case class Success(content: String) extends Message
final case class Fail(content: String) extends Message
