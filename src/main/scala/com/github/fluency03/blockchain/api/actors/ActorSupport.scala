package com.github.fluency03.blockchain.api.actors

import akka.actor.{Actor, ActorLogging}
import akka.util.Timeout

import scala.concurrent.duration._

trait ActorSupport extends Actor with ActorLogging  {

  // Required by the `ask` (?) method
  // usually we'd obtain the timeout from the system's configuration
  implicit lazy val timeout: Timeout = Timeout(5.seconds)

}
