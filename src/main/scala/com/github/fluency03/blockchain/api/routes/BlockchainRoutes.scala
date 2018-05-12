package com.github.fluency03.blockchain.api.routes

import akka.actor.ActorRef
import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.unmarshalling.PredefinedFromStringUnmarshallers.CsvSeq
import akka.pattern.ask
import com.github.fluency03.blockchain.api.{Input, Message}
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
          onSuccess((blockchainActor ? CheckBlockchainValidity).mapTo[Message]) {
            respondOnUpdate
          }
        }
      } ~
      path(LAST_BLOCK) {
        get {
          rejectEmptyResponse {
            complete((blockchainActor ? GetLastBlock).mapTo[Option[Block]])
          }
        } ~
        delete {
          onSuccess((blockchainActor ? RemoveLastBlock).mapTo[Message]) {
            respondOnUpdate
          }
        }
      } ~
      path(NEW_BLOCK) {
        parameters('hash.?) {
          case Some(hash) =>
            onSuccess((blockchainActor ? AppendBlockFromPool(hash)).mapTo[Message]) {
              respondOnUpdate
            }
          case None => entity(as[Block]) { block =>
            onSuccess((blockchainActor ? AppendBlock(block)).mapTo[Message]) {
              respondOnUpdate
            }
          }
        }
      } ~
      path(NEXT_BLOCK) {
        post {
          parameters('id.as(CsvSeq[String]).?) { idsOpt: Option[Seq[String]] =>
            entity(as[Input]) { in =>
              val maybeNextBlock: Future[Option[Block]] =
                (blockchainActor ? MineNextBlock(in.content, idsOpt.getOrElse(Seq.empty[String])))
                  .mapTo[Option[Block]]
              rejectEmptyResponse {
                complete(maybeNextBlock)
              }
            }
          }
        }
      } ~
      pathEnd {
        get {
          rejectEmptyResponse {
            complete((blockchainActor ? GetBlockchain).mapTo[Option[Blockchain]])
          }
        } ~
        post {
          onSuccess((blockchainActor ? CreateBlockchain).mapTo[Message]) {
            respondOnCreation
          }
        } ~
        delete {
          onSuccess((blockchainActor ? DeleteBlockchain).mapTo[Message]) {
            respondOnDeletion
          }
        }
      } ~
      path(BLOCKS) {
        get {
          parameters('hashes.as(CsvSeq[String]).?, 'indices.as(CsvSeq[Int]).?) {
            (hashesOpt: Option[Seq[String]], indicesOpt: Option[Seq[Int]]) =>
              val (hashes, indices) = (hashesOpt.getOrElse(Seq.empty[String]).toSet,
                indicesOpt.getOrElse(Seq.empty[Int]).toSet)
              val blocks: Future[Set[Block]] =
                (blockchainActor ? GetBlocksByHashesAndIndices(hashes, indices)).mapTo[Set[Block]]
              rejectEmptyResponse {
                complete(blocks)
              }
          }
        }
      } ~
      pathPrefix(BLOCK / Segment) { hash =>
        pathEnd {
          get {
            rejectEmptyResponse {
              complete((blockchainActor ? GetBlockByHash(hash)).mapTo[Option[Block]])
            }
          }
        } ~
        path(TRANSACTION / Segment) { id =>
          get {
            rejectEmptyResponse {
              complete((blockchainActor ? GetTxOfBlock(id, hash)).mapTo[Option[Transaction]])
            }
          }
        }
      }
    }

}
