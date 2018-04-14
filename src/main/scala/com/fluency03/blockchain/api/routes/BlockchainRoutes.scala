package com.fluency03.blockchain.api.routes

import akka.actor.ActorRef
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import com.fluency03.blockchain.api.JsonSupport
import com.fluency03.blockchain.api.actors.BlockchainRegistryActor._
import com.fluency03.blockchain.core.Blockchain
import org.json4s.JsonAST.JValue

import scala.concurrent.Future

trait BlockchainRoutes extends JsonSupport with Routes {
  lazy val logBlockchainRoutes = Logging(system, classOf[BlockchainRoutes])

  def blockchainRegistryActor: ActorRef

  lazy val blockchainRoutes: Route =
    pathPrefix("blockchain") {
      pathEnd {
        get {
          val users: Future[Blockchain] =
            (blockchainRegistryActor ? GetBlockchain).mapTo[Blockchain]
          complete(users)
        } ~
        post {
          entity(as[JValue]) { _ =>
            val blockchainCreated: Future[ActionPerformed] =
              (blockchainRegistryActor ? CreateBlockchain).mapTo[ActionPerformed]
            onSuccess(blockchainCreated) { performed =>
              logBlockchainRoutes.info("Created Blockchain: {}", performed.description)
              complete((StatusCodes.Created, performed))
            }
          }
        } ~
        delete {
          val blockchainDeleted: Future[ActionPerformed] =
            (blockchainRegistryActor ? DeleteBlockchain).mapTo[ActionPerformed]
          onSuccess(blockchainDeleted) { performed =>
            logBlockchainRoutes.info("Deleted Blockchain: {}", performed.description)
            complete((StatusCodes.OK, performed))
          }
        }
      }
    }
}
