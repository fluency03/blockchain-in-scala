package com.github.fluency03.blockchain
package api.routes

import akka.actor.ActorRef
import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.unmarshalling.PredefinedFromStringUnmarshallers.CsvSeq
import akka.pattern.ask
import com.github.fluency03.blockchain.api.Message
import com.github.fluency03.blockchain.api.actors.NetworkActor._
import com.github.fluency03.blockchain.core.{Peer, PeerSimple}

import scala.concurrent.Future

trait NetworkRoutes extends RoutesSupport {
  lazy val log = Logging(system, classOf[NetworkRoutes])

  def networkActor: ActorRef

  /**
   * TODO (Chang):
   *  - Sign transaction
   *
   */

  lazy val networkRoutes: Route =
    path(NETWORK) {
      get {
        complete((networkActor ? GetNetwork).mapTo[Set[String]])
      }
    } ~
    path(PEERS) {
      get {
        parameters( 'names.as(CsvSeq[String]).? ) { namesOpt =>
          val peers: Future[Map[String, Set[HexString]]] = namesOpt match {
            case Some(names) =>
              (networkActor ? GetPeers(names.toSet)).mapTo[Map[String, Set[HexString]]]
            case None => (networkActor ? GetPeers).mapTo[Map[String, Set[HexString]]]
          }
          complete(peers)
        }
      }
    } ~
    pathPrefix(PEER) {
      pathEnd {
        post {
          entity(as[PeerSimple]) { peer =>
            onSuccess((networkActor ? CreatePeer(peer.name)).mapTo[Message]) {
              respondOnCreation
            }
          }
        }
      } ~
      path(Segment) { name =>
        get {
          rejectEmptyResponse {
            complete((networkActor ? GetPeer(name)).mapTo[Option[Peer]])
          }
        } ~
        delete {
          onSuccess((networkActor ? DeletePeer(name)).mapTo[Message]) {
            respondOnDeletion
          }
        }
      }
    }

}
