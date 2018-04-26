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
import com.fluency03.blockchain.api.{FailureMsg, Message, SuccessMsg}
import com.fluency03.blockchain.core.{Peer, PeerSimple}

import scala.concurrent.Future

trait NetworkRoutes extends RoutesSupport {
  lazy val log = Logging(system, classOf[NetworkRoutes])

  def networkActor: ActorRef

  lazy val networkRoutes: Route =
    path("peers") {
      get {
        val peers: Future[Set[String]] = (networkActor ? GetPeers).mapTo[Set[String]]
        complete(peers)
      }
    } ~
    pathPrefix("peer") {
      pathEnd {
        post {
          entity(as[PeerSimple]) { peer =>
            val peerCreated: Future[Message] = (networkActor ? CreatePeer(peer.name)).mapTo[Message]
            onSuccess(peerCreated) {
              case s: SuccessMsg => complete((StatusCodes.Created, s))
              case f: FailureMsg => complete((StatusCodes.Conflict, f))
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
              case s: SuccessMsg => complete((StatusCodes.OK, s))
              case f: FailureMsg => complete((StatusCodes.NotFound, f))
            }
          }
      }
    }

}
