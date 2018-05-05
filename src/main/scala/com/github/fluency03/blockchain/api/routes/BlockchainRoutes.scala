package com.github.fluency03.blockchain.api.routes

import akka.actor.ActorRef
import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.unmarshalling.PredefinedFromStringUnmarshallers.CsvSeq
import akka.pattern.ask
import com.github.fluency03.blockchain.api.{FailureMsg, Input, Message}
import com.github.fluency03.blockchain.api.actors.BlockchainActor._
import com.github.fluency03.blockchain.core.{Block, Blockchain, Transaction}

import scala.concurrent.Future

trait BlockchainRoutes extends RoutesSupport {
  lazy val log = Logging(system, classOf[BlockchainRoutes])

  def blockchainActor: ActorRef

  lazy val blockchainRoutes: Route =
    pathPrefix(BLOCKCHAIN) {
      path(VALIDITY) {
        get {
          val validity: Future[Message] = (blockchainActor ? CheckBlockchainValidity).mapTo[Message]
          onSuccess(validity) { respondOnUpdate }
        }
      } ~
      path(LAST_BLOCK) {
        get {
          val maybeLastBlock: Future[Option[Block]] = (blockchainActor ? GetLastBlock).mapTo[Option[Block]]
          rejectEmptyResponse { complete(maybeLastBlock) }
        } ~
        delete {
          val blockchainUpdated: Future[Message] = (blockchainActor ? RemoveLastBlock).mapTo[Message]
          onSuccess(blockchainUpdated) { respondOnUpdate }
        }
      } ~
      path(NEW_BLOCK) {
        parameters('hash.?) {
          case Some(hash) =>
            val blockchainUpdated: Future[Message] = (blockchainActor ? AppendBlockFromPool(hash)).mapTo[Message]
            onSuccess(blockchainUpdated) { respondOnUpdate }
          case None => entity(as[Block]) { block =>
            val blockchainUpdated: Future[Message] = (blockchainActor ? AppendBlock(block)).mapTo[Message]
            onSuccess(blockchainUpdated) { respondOnUpdate }
          }
        }
      } ~
      path(NEXT_BLOCK) {
        post {
          parameters('id.as(CsvSeq[String]).?) { idsOpt: Option[Seq[String]] =>
            entity(as[Input]) { in =>
              val maybeNextBlock: Future[Option[Block]] =
                (blockchainActor ? MineNextBlock(in.content, idsOpt.getOrElse(Seq.empty[String]))).mapTo[Option[Block]]
              rejectEmptyResponse { complete(maybeNextBlock) }
            }
          }
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
            val maybeTx: Future[Option[Transaction]] = (blockchainActor ? GetTxOfBlock(id, hash)).mapTo[Option[Transaction]]
            rejectEmptyResponse { complete(maybeTx) }
          }
        }
      }
    }

}
