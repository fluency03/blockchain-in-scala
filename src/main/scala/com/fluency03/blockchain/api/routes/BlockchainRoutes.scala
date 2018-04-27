package com.fluency03.blockchain.api.routes

import akka.actor.ActorRef
import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import com.fluency03.blockchain.api.Message
import com.fluency03.blockchain.api.actors.BlockchainActor._
import com.fluency03.blockchain.core.Blockchain

import scala.concurrent.Future

trait BlockchainRoutes extends RoutesSupport {
  lazy val log = Logging(system, classOf[BlockchainRoutes])

  def blockchainActor: ActorRef

  lazy val blockchainRoutes: Route =
    pathPrefix(BLOCKCHAIN) {
      pathEnd {
        get {
          val blockchain: Future[Option[Blockchain]] = (blockchainActor ? GetBlockchain).mapTo[Option[Blockchain]]
          rejectEmptyResponse { complete(blockchain) }
        } ~
        post {
          val blockchainCreated: Future[Message] = (blockchainActor ? CreateBlockchain).mapTo[Message]
          onSuccess(blockchainCreated) { respondOnCreation }
        } ~
        delete {
          val blockchainDeleted: Future[Message] = (blockchainActor ? DeleteBlockchain).mapTo[Message]
          onSuccess(blockchainDeleted) { respondOnDeletion }
        }
      }
    }
}
