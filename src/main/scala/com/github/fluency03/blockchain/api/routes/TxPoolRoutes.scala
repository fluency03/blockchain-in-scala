package com.github.fluency03.blockchain
package api.routes

import akka.actor.ActorRef
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.unmarshalling.PredefinedFromStringUnmarshallers.CsvSeq
import akka.pattern.ask
import com.github.fluency03.blockchain.api.actors.TxPoolActor._
import com.github.fluency03.blockchain.api.{FailureMsg, Message, Transactions}
import com.github.fluency03.blockchain.core.Transaction

import scala.concurrent.Future

trait TxPoolRoutes extends RoutesSupport {
  lazy val log = Logging(system, classOf[TxPoolRoutes])

  def txPoolActor: ActorRef

  /**
   * TODO (Chang):
   *  - Sign transaction
   *
   */

  lazy val txPoolRoutes: Route =
    pathPrefix(TX_POOL) {
      path(TRANSACTIONS) {
        parameters('ids.as(CsvSeq[HexString]).?) { idsOpt =>
          val transactions: Future[Transactions] = idsOpt match {
            case Some(ids) => (txPoolActor ? GetTransactions(ids)).mapTo[Transactions]
            case None => (txPoolActor ? GetTransactions).mapTo[Transactions]
          }
          complete(transactions)
        }
      } ~
      pathPrefix(TRANSACTION) {
        pathEnd {
          post {
            entity(as[Transaction]) { tx =>
              onSuccess((txPoolActor ? AddTransaction(tx)).mapTo[Message]) {
                respondOnCreation
              }
            }
          }
        } ~
        path(Segment) { id =>
          get {
            rejectEmptyResponse {
              complete((txPoolActor ? GetTransaction(id)).mapTo[Option[Transaction]])
            }
          } ~
          delete {
            onSuccess((txPoolActor ? DeleteTransaction(id)).mapTo[Message]) {
              respondOnDeletion
            }
          } ~
          put {
            entity(as[Transaction]) { tx =>
              if (tx.id != id) {
                val code = StatusCodes.InternalServerError
                val msg = FailureMsg("Transaction ID in the data does not match ID on the path.")
                complete((code, msg))
              } else onSuccess((txPoolActor ? UpdateTransaction(tx)).mapTo[Message]) {
                respondOnUpdate
              }
            }
          }
        }
      }
    }
}
