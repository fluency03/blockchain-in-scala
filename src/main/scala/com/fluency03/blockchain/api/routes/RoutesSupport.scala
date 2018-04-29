package com.fluency03.blockchain.api.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{RequestContext, StandardRoute}
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.util.Timeout
import com.fluency03.blockchain.api.{FailureMsg, JsonSupport, Message, SuccessMsg}

import scala.concurrent.Future
import scala.concurrent.duration._

trait RoutesSupport extends JsonSupport {
  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  // Required by the `ask` (?) method
  implicit lazy val timeout: Timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  def respondOnCreation(m: Message): StandardRoute = m match {
    case s: SuccessMsg => complete((StatusCodes.Created, s))
    case f: FailureMsg => complete((StatusCodes.Conflict, f))
  }

  def respondOnDeletion(m: Message): StandardRoute = m match {
    case s: SuccessMsg => complete((StatusCodes.OK, s))
    case f: FailureMsg => complete((StatusCodes.NotFound, f))
  }

  def respondOnUpdate(m: Message): StandardRoute = m match {
    case s: SuccessMsg => complete((StatusCodes.OK, s))
    case f: FailureMsg => complete((StatusCodes.InternalServerError, f))
  }

}
