package com.fluency03.blockchain.api.routes

import akka.actor.ActorSystem
import com.fluency03.blockchain.api.utils.JsonSupport

trait Routes extends JsonSupport {
  // we leave these abstract, since they will be provided by the App
//  implicit def system: ActorSystem

}
