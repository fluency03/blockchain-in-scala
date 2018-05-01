package com.fluency03.blockchain.api.routes

import akka.actor.ActorRef
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.unmarshalling.PredefinedFromStringUnmarshallers.CsvSeq
import akka.pattern.ask
import com.fluency03.blockchain.api.{FailureMsg, Input, Message}
import com.fluency03.blockchain.api.actors.BlockchainActor._
import com.fluency03.blockchain.core.{Block, Blockchain, Transaction}

import scala.concurrent.Future

trait BlockchainRoutes extends RoutesSupport {
  lazy val log = Logging(system, classOf[BlockchainRoutes])

  def blockchainActor: ActorRef

  /**
   * TODO (Chang):
   *
   */

  lazy val blockchainRoutes: Route =
    pathPrefix(BLOCKCHAIN) {
      path(VALIDITY) {
        get {
          val validity: Future[Message] = (blockchainActor ? CheckBlockchainValidity).mapTo[Message]
          onSuccess(validity) { respondOnUpdate }
        }
      } ~
      pathEnd {
        get {
          val blockchain: Future[Option[Blockchain]] = (blockchainActor ? GetBlockchain).mapTo[Option[Blockchain]]
          rejectEmptyResponse { complete(blockchain) }
        } ~
        post {
          val blockchainCreated: Future[Message] = (blockchainActor ? CreateBlockchain).mapTo[Message]
          onSuccess(blockchainCreated) { respondOnCreation }
        } ~
        put {
          parameters('action, 'hash.?, 'id.as(CsvSeq[String]).?) {
            (action, hashOpt: Option[String], idsOpt: Option[Seq[String]]) => action match {
              case ADD_BLOCK_ACTION => hashOpt match {
                case Some(hash) =>
                  val blockchainUpdated: Future[Message] = (blockchainActor ? AddBlockFromPool(hash)).mapTo[Message]
                  onSuccess(blockchainUpdated) { respondOnUpdate }
                case None => entity(as[Block]) { block =>
                  val blockchainUpdated: Future[Message] = (blockchainActor ? AddBlock(block)).mapTo[Message]
                  onSuccess(blockchainUpdated) { respondOnUpdate }
                }
              }
              case REMOVE_BLOCK_ACTION =>
                val blockchainUpdated: Future[Message] = (blockchainActor ? RemoveBlock).mapTo[Message]
                onSuccess(blockchainUpdated) { respondOnUpdate }
              case MINE_NEXT_BLOCK_ACTION => entity(as[Input]) { in =>
                val maybeNextBlock: Future[Option[Block]] =
                  (blockchainActor ? MineNextBlock(in.content, idsOpt.getOrElse(Seq.empty[String]))).mapTo[Option[Block]]
                rejectEmptyResponse { complete(maybeNextBlock) }
              }
              case act => complete((StatusCodes.BadRequest, FailureMsg(s"Action Not Supported: $act")))
            }
          }
        } ~
        delete {
          val blockchainDeleted: Future[Message] = (blockchainActor ? DeleteBlockchain).mapTo[Message]
          onSuccess(blockchainDeleted) { respondOnDeletion }
        }
      } ~
      pathPrefix(BLOCK / Segment) { hash =>
        pathEnd {
          get {
            val maybeBlock: Future[Option[Block]] = (blockchainActor ? GetBlockFromChain(hash)).mapTo[Option[Block]]
            rejectEmptyResponse { complete(maybeBlock) }
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
