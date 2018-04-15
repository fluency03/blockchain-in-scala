package com.fluency03.blockchain.api.routes

import akka.actor.ActorSystem
import akka.util.Timeout
import com.fluency03.blockchain.api.utils.JsonSupport

import scala.concurrent.duration._

trait Routes extends JsonSupport {
  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  // Required by the `ask` (?) method
  implicit lazy val timeout: Timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

}
