package com.github.fluency03.blockchain
package api.routes

import akka.actor.ActorRef
import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.unmarshalling.PredefinedFromStringUnmarshallers.CsvSeq
import akka.pattern.ask
import com.github.fluency03.blockchain.api.{Blocks, Input, Message}
import com.github.fluency03.blockchain.api.actors.BlockPoolActor._
import com.github.fluency03.blockchain.core.{Block, Transaction}

import scala.concurrent.Future

trait BlockPoolRoutes extends RoutesSupport {
  lazy val log = Logging(system, classOf[BlockPoolRoutes])

  def blockPoolActor: ActorRef

  lazy val blockPoolRoutes: Route =
    pathPrefix(BLOCK_POOL) {
      path(BLOCKS) {
        parameters('hashes.as(CsvSeq[HexString]).?) { hashesOpt: Option[Seq[HexString]] =>
          val blocks: Future[Blocks] = hashesOpt match {
            case Some(hashes) => (blockPoolActor ? GetBlocks(hashes.toSet)).mapTo[Blocks]
            case None => (blockPoolActor ? GetBlocks).mapTo[Blocks]
          }
          complete(blocks)
        }
      } ~
      path(NEXT_BLOCK) {
        post {
          parameters('ids.as(CsvSeq[HexString]).?) { idsOpt: Option[Seq[HexString]] =>
            entity(as[Input]) { in =>
              val action = MineAndAddNextBlock(in.content, idsOpt.getOrElse(Seq.empty[HexString]))
              val maybeNextBlock: Future[Option[Block]] =
                (blockPoolActor ? action).mapTo[Option[Block]]
              rejectEmptyResponse {
                complete(maybeNextBlock)
              }
            }
          }
        }
      } ~
      pathPrefix(BLOCK) {
        pathEnd {
          post {
            entity(as[Block]) { block =>
              onSuccess((blockPoolActor ? AddBlock(block)).mapTo[Message]) {
                respondOnCreation
              }
            }
          }
        } ~
        pathPrefix(Segment) { hash =>
          pathEnd {
            get {
              rejectEmptyResponse {
                complete((blockPoolActor ? GetBlock(hash)).mapTo[Option[Block]])
              }
            } ~
            delete {
              onSuccess((blockPoolActor ? DeleteBlock(hash)).mapTo[Message]) {
                respondOnDeletion
              }
            }
          } ~
          path(TRANSACTION / Segment) { id =>
            get {
              rejectEmptyResponse {
                complete((blockPoolActor ? GetTxOfBlock(id, hash)).mapTo[Option[Transaction]])
              }
            }
          }
        }
      }
    }

}
