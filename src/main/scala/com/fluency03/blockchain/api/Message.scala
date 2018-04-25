package com.fluency03.blockchain.api

final case class Input(content: String)

sealed trait Message
final case class SuccessMsg(content: String) extends Message
final case class FailureMsg(content: String) extends Message
