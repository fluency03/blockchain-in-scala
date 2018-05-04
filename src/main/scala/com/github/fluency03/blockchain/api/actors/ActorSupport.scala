package com.github.fluency03.blockchain.api.actors

import akka.actor.{Actor, ActorLogging}
import akka.util.Timeout

import scala.concurrent.duration._

trait ActorSupport extends Actor with ActorLogging  {

  // Required by the `ask` (?) method
  implicit lazy val timeout: Timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

}
