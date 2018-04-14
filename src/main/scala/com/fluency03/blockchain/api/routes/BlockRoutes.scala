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
import com.fluency03.blockchain.api.JsonSupport
import com.fluency03.blockchain.api.actors.BlockRegistryActor._
import com.fluency03.blockchain.core.Block

import scala.concurrent.Future

trait BlockRoutes extends JsonSupport with Routes {
  lazy val logBlockRoutes = Logging(system, classOf[BlockRoutes])

  def blockRegistryActor: ActorRef

  lazy val blockRoutes: Route =
    pathPrefix("blocks") {
      pathEnd {
        get {
          val blocks: Future[List[Block]] =
            (blockRegistryActor ? GetBlocks).mapTo[List[Block]]
          complete(blocks)
        } ~
        post {
          entity(as[Block]) { block =>
            val blockCreated: Future[ActionPerformed] =
              (blockRegistryActor ? CreateBlock(block)).mapTo[ActionPerformed]
            onSuccess(blockCreated) { performed =>
              logBlockRoutes.info("Created user [{}]: {}", block.hash, performed.description)
              complete((StatusCodes.Created, performed))
            }
          }
        }
      } ~
      path(Segment) { hash =>
        get {
          val maybeBlock: Future[Option[Block]] =
            (blockRegistryActor ? GetBlock(hash)).mapTo[Option[Block]]
          rejectEmptyResponse {
            complete(maybeBlock)
          }
        } ~
        delete {
          val blockDeleted: Future[ActionPerformed] =
            (blockRegistryActor ? DeleteBlock(hash)).mapTo[ActionPerformed]
          onSuccess(blockDeleted) { performed =>
            logBlockRoutes.info("Deleted block [{}]: {}", hash, performed.description)
            complete((StatusCodes.OK, performed))
          }
        }
      }
    }
}
