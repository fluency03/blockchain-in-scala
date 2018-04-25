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
import com.fluency03.blockchain.api.actors.NetworkActor.{CreatePeer, DeletePeer, GetPeer, GetPeers}
import com.fluency03.blockchain.api.{Fail, Message, Success}
import com.fluency03.blockchain.core.{Peer, PeerInput}

import scala.concurrent.Future

trait NetworkRoutes extends Routes {
  lazy val log = Logging(system, classOf[NetworkRoutes])

  def networkActor: ActorRef

  lazy val networkRoutes: Route =
    path("peers") {
      get {
        val peers: Future[Seq[String]] = (networkActor ? GetPeers).mapTo[Seq[String]]
        complete(peers)
      }
    } ~
    pathPrefix("peer") {
      pathEnd {
        post {
          entity(as[PeerInput]) { peerInput =>
            val blockchainCreated: Future[Message] = (networkActor ? CreatePeer(peerInput.name)).mapTo[Message]
            onSuccess(blockchainCreated) {
              case Success(content) => complete((StatusCodes.Created, content))
              case Fail(content) => complete((StatusCodes.Conflict, content))
            }
          }
        }
      } ~
      path(Segment) { name =>
        get {
          val maybePeer: Future[Option[Peer]] = (networkActor ? GetPeer(name)).mapTo[Option[Peer]]
          rejectEmptyResponse { complete(maybePeer) }
        } ~
          delete {
            val peerDeleted: Future[Message] = (networkActor ? DeletePeer(name)).mapTo[Message]
            onSuccess(peerDeleted) {
              case Success(content) => complete((StatusCodes.OK, content))
              case Fail(content) => complete((StatusCodes.NotFound, content))
            }
          }
      }
    }

}
