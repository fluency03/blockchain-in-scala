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
import com.fluency03.blockchain.core.{Block, Blockchain, Transaction}

import scala.concurrent.Future

trait BlockchainRoutes extends RoutesSupport {
  lazy val log = Logging(system, classOf[BlockchainRoutes])

  def blockchainActor: ActorRef

  /**
   * TODO (Chang): new APIS:
   *  - AddBlockOnBlockchain
   *  - CheckBlockchainIsValid
   *  - MineNextBlock
   *
   */

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
      } ~
      pathPrefix(BLOCK / Segment) { hash =>
        pathEnd {
          get {
            val maybeBlock: Future[Option[Block]] = (blockchainActor ? GetBlock(hash)).mapTo[Option[Block]]
            rejectEmptyResponse {
              complete(maybeBlock)
            }
          }
        } ~
        path(TRANSACTION / Segment) { id =>
          get {
            log.info(s"$id; $hash")
            val maybeTx: Future[Option[Transaction]] = (blockchainActor ? GetTxOfBlock(id, hash)).mapTo[Option[Transaction]]
            rejectEmptyResponse { complete(maybeTx) }
          }
        }
      }
    }
}
