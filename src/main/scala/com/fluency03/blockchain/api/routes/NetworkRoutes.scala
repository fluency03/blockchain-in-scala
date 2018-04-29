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
import com.fluency03.blockchain.api.Message
import com.fluency03.blockchain.api.actors.NetworkActor._
import com.fluency03.blockchain.core.{Peer, PeerSimple}

import scala.concurrent.Future

trait NetworkRoutes extends RoutesSupport {
  lazy val log = Logging(system, classOf[NetworkRoutes])

  def networkActor: ActorRef

  lazy val networkRoutes: Route =
    path(NETWORK) {
      get {
        val network: Future[Set[String]] = (networkActor ? GetNetwork).mapTo[Set[String]]
        complete(network)
      }
    } ~
    path(PEERS) {
      get {
        parameters( 'names.as(CsvSeq[String]) ? ) { names =>
          val peers: Future[Map[String, Set[String]]] =
            if (names.isDefined) (networkActor ? GetPeers(names.get.toSet)).mapTo[Map[String, Set[String]]]
            else (networkActor ? GetPeers).mapTo[Map[String, Set[String]]]
          complete(peers)
        }
      }
    } ~
    pathPrefix(PEER) {
      pathEnd {
        post {
          entity(as[PeerSimple]) { peer =>
            val peerCreated: Future[Message] = (networkActor ? CreatePeer(peer.name)).mapTo[Message]
            onSuccess(peerCreated) { respondOnCreation }
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
          onSuccess(peerDeleted) { respondOnDeletion }
        }
      }
    }

}
