package com.github.fluency03.blockchain.api

final case class Input(content: String)

sealed trait Message
final case class SuccessMsg(message: String) extends Message
final case class FailureMsg(error: String) extends Message
