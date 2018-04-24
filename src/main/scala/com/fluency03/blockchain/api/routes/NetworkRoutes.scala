package com.fluency03.blockchain.api.routes

import akka.actor.ActorRef
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import com.fluency03.blockchain.api.Blocks
import com.fluency03.blockchain.api.actors.BlocksActor._
import com.fluency03.blockchain.api.utils.GenericMessage.Response
import com.fluency03.blockchain.core.Block

import scala.concurrent.Future

trait NetworkRoutes extends Routes {
  lazy val log = Logging(system, classOf[NetworkRoutes])

  def networkActor: ActorRef

  lazy val networkRoutes: Route = ???

}
