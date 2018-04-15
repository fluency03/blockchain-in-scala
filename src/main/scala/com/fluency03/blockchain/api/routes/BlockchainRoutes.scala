package com.fluency03.blockchain.api.routes

import akka.actor.ActorRef
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import com.fluency03.blockchain.api.actors.BlockchainActor._
import com.fluency03.blockchain.api.utils.GenericMessage.Response
import com.fluency03.blockchain.core.Blockchain
import org.json4s.JsonAST.JValue

import scala.concurrent.Future

trait BlockchainRoutes extends Routes {
  lazy val log = Logging(system, classOf[BlockchainRoutes])

  def blockchainActor: ActorRef

  lazy val blockchainRoutes: Route =
    pathPrefix("blockchain") {
      pathEnd {
        get {
          val blockchain: Future[Option[Blockchain]] = (blockchainActor ? GetBlockchain).mapTo[Option[Blockchain]]
          rejectEmptyResponse { complete(blockchain) }
        } ~
        post {
          entity(as[JValue]) { _ =>
            val blockchainCreated: Future[Response] = (blockchainActor ? CreateBlockchain).mapTo[Response]
            onSuccess(blockchainCreated) { resp =>
              log.info("Created Blockchain: {}", resp.message)
              complete((StatusCodes.Created, resp))
            }
          }
        } ~
        delete {
          val blockchainDeleted: Future[Response] = (blockchainActor ? DeleteBlockchain).mapTo[Response]
          onSuccess(blockchainDeleted) { resp =>
            log.info("Deleted Blockchain: {}", resp.message)
            complete((StatusCodes.OK, resp))
          }
        }
      }
    }
}
