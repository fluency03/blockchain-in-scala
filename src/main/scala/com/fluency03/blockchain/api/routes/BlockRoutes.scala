package com.fluency03.blockchain.api.routes

import akka.actor.ActorRef
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import com.fluency03.blockchain.api.{Blocks, FailureMsg, Message, SuccessMsg}
import com.fluency03.blockchain.api.actors.BlocksActor._
import com.fluency03.blockchain.core.Block

import scala.concurrent.Future

trait BlockRoutes extends RoutesSupport {
  lazy val log = Logging(system, classOf[BlockRoutes])

  def blocksActor: ActorRef

  /**
   * TODO (Chang): new APIS:
   *  - CreateBlock
   *  - GetBlock (onChain or offChain)
   *  - GetTransactionOfABlock
   *  - AddBlockOnChain
   *
   */

  lazy val blockRoutes: Route =
    path(BLOCKS) {
      get {
        val blocks: Future[Blocks] = (blocksActor ? GetBlocks).mapTo[Blocks]
        complete(blocks)
      }
    } ~
    pathPrefix(BLOCK) {
      pathEnd {
        post {
          entity(as[Block]) { block =>
            val blockCreated: Future[Message] = (blocksActor ? CreateBlock(block)).mapTo[Message]
            onSuccess(blockCreated) { respondOnCreation }
          }
        }
      } ~
      path(Segment) { hash =>
        get {
          val maybeBlock: Future[Option[Block]] = (blocksActor ? GetBlock(hash)).mapTo[Option[Block]]
          rejectEmptyResponse { complete(maybeBlock) }
        } ~
        delete {
          val blockDeleted: Future[Message] = (blocksActor ? DeleteBlock(hash)).mapTo[Message]
          onSuccess(blockDeleted) { respondOnDeletion }
        }
      }
    }
}
