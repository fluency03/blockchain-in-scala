package com.github.fluency03.blockchain.api.routes

import akka.actor.ActorRef
import akka.event.Logging
import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.unmarshalling.PredefinedFromStringUnmarshallers.CsvSeq
import akka.pattern.ask
import com.github.fluency03.blockchain.api.{Blocks, Input, Message}
import com.github.fluency03.blockchain.api.actors.BlockPoolActor._
import com.github.fluency03.blockchain.api.actors.BlockchainActor
import com.github.fluency03.blockchain.core.{Block, Transaction}

import scala.concurrent.Future

trait BlockPoolRoutes extends RoutesSupport {
  lazy val log = Logging(system, classOf[BlockPoolRoutes])

  def blockPoolActor: ActorRef

  /**
   * TODO (Chang): new APIS:
   *  - CreateBlock
   *
   */

  lazy val blockPoolRoutes: Route =
    pathPrefix(BLOCK_POOL) {
      path(BLOCKS) {
        parameters('hashes.as(CsvSeq[String]).?) { hashesOpt: Option[Seq[String]] =>
          val blocks: Future[Blocks] = hashesOpt match {
            case Some(hashes) => (blockPoolActor ? GetBlocks(hashes.toSet)).mapTo[Blocks]
            case None => (blockPoolActor ? GetBlocks).mapTo[Blocks]
          }
          complete(blocks)
        }
      } ~
      path(NEXT_BLOCK) {
        post {
          parameters('id.as(CsvSeq[String]).?) { idsOpt: Option[Seq[String]] =>
            entity(as[Input]) { in =>
              val maybeNextBlock: Future[Option[Block]] =
                (blockPoolActor ? MineAndAddNextBlock(in.content, idsOpt.getOrElse(Seq.empty[String]))).mapTo[Option[Block]]
              rejectEmptyResponse { complete(maybeNextBlock) }
            }
          }
        }
      } ~
      pathPrefix(BLOCK) {
        pathEnd {
          post {
            entity(as[Block]) { block =>
              val blockCreated: Future[Message] = (blockPoolActor ? AddBlock(block)).mapTo[Message]
              onSuccess(blockCreated) { respondOnCreation }
            }
          }
        } ~
        pathPrefix(Segment) { hash =>
          pathEnd {
            get {
              val maybeBlock: Future[Option[Block]] = (blockPoolActor ? GetBlock(hash)).mapTo[Option[Block]]
              rejectEmptyResponse { complete(maybeBlock) }
            } ~
            delete {
              val blockDeleted: Future[Message] = (blockPoolActor ? DeleteBlock(hash)).mapTo[Message]
              onSuccess(blockDeleted) { respondOnDeletion }
            }
          } ~
          path(TRANSACTION / Segment) { id =>
            get {
              val maybeTx: Future[Option[Transaction]] = (blockPoolActor ? GetTxOfBlock(id, hash)).mapTo[Option[Transaction]]
              rejectEmptyResponse { complete(maybeTx) }
            }
          }
        }
      }
    }

}
