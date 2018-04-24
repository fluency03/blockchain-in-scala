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
import com.fluency03.blockchain.api.Blocks
import com.fluency03.blockchain.api.actors.BlocksActor._
import com.fluency03.blockchain.api.utils.GenericMessage.Response
import com.fluency03.blockchain.core.Block

import scala.concurrent.Future

trait BlockRoutes extends Routes {
  lazy val log = Logging(system, classOf[BlockRoutes])

  def blocksActor: ActorRef

  lazy val blockRoutes: Route =
    path("blocks") {
      get {
        val blocks: Future[Blocks] = (blocksActor ? GetBlocks).mapTo[Blocks]
        complete(blocks)
      }
    } ~
    pathPrefix("block") {
      pathEnd {
        post {
          entity(as[Block]) { block =>
            val blockCreated: Future[Response] = (blocksActor ? CreateBlock(block)).mapTo[Response]
            onSuccess(blockCreated) { resp =>
              log.info("Created block [{}]: {}", block.hash, resp.message)
              complete((StatusCodes.Created, resp))
            }
          }
        }
      } ~
      path(Segment) { hash =>
        get {
          val maybeBlock: Future[Option[Block]] = (blocksActor ? GetBlock(hash)).mapTo[Option[Block]]
          rejectEmptyResponse { complete(maybeBlock) }
        } ~
        delete {
          val blockDeleted: Future[Response] = (blocksActor ? DeleteBlock(hash)).mapTo[Response]
          onSuccess(blockDeleted) { resp =>
            log.info("Deleted block [{}]: {}", hash, resp.message)
            complete((StatusCodes.OK, resp))
          }
        }
      }
    }
}
