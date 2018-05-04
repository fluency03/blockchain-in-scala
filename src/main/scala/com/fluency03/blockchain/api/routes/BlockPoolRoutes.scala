package com.fluency03.blockchain.api.routes

import akka.actor.ActorRef
import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.unmarshalling.PredefinedFromStringUnmarshallers.CsvSeq
import akka.pattern.ask
import com.fluency03.blockchain.api.{Blocks, Message}
import com.fluency03.blockchain.api.actors.BlockPoolActor._
import com.fluency03.blockchain.core.Block

import scala.concurrent.Future

trait BlockPoolRoutes extends RoutesSupport {
  lazy val log = Logging(system, classOf[BlockPoolRoutes])

  def blockPoolActor: ActorRef

  /**
   * TODO (Chang): new APIS:
   *  - CreateBlock
   *  - GetBlock (onChain or offChain)
   *  - GetTransactionOfABlock
   *  - ContainsBlock
   *  - AddBlockOnChain
   *
   */

  lazy val blockPoolRoutes: Route =
    path(BLOCKS) {
      parameters( 'hashes.as(CsvSeq[String]).? ) { hashesOpt =>
        val blocks: Future[Blocks] = hashesOpt match {
          case Some(hashes) => (blockPoolActor ? GetBlocks(hashes.toSet)).mapTo[Blocks]
          case None => (blockPoolActor ? GetBlocks).mapTo[Blocks]
        }
        complete(blocks)
      }
    } ~
    pathPrefix(BLOCK) {
      pathEnd {
        post {
          entity(as[Block]) { block =>
            val blockCreated: Future[Message] = (blockPoolActor ? CreateBlock(block)).mapTo[Message]
            onSuccess(blockCreated) { respondOnCreation }
          }
        }
      } ~
      path(Segment) { hash =>
        get {
          val maybeBlock: Future[Option[Block]] = (blockPoolActor ? GetBlock(hash)).mapTo[Option[Block]]
          rejectEmptyResponse { complete(maybeBlock) }
        } ~
        delete {
          val blockDeleted: Future[Message] = (blockPoolActor ? DeleteBlock(hash)).mapTo[Message]
          onSuccess(blockDeleted) { respondOnDeletion }
        }
      }
    }

}