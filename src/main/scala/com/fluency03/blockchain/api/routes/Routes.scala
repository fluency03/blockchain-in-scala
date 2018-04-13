package com.fluency03.blockchain.api.routes

import akka.actor.ActorSystem
import akka.event.Logging
import akka.util.Timeout

import scala.concurrent.duration._

trait Routes {
  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  // Required by the `ask` (?) method below
  implicit lazy val timeout: Timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration


}
