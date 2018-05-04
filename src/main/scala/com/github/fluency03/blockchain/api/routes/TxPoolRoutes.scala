package com.github.fluency03.blockchain.api.routes

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
    path(TRANSACTIONS) {
      parameters( 'ids.as(CsvSeq[String]).? ) { idsOpt =>
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
            val msgOnCreate: Future[Message] = (txPoolActor ? AddTransaction(tx)).mapTo[Message]
            onSuccess(msgOnCreate) { respondOnCreation }
          }
        }
      } ~
      path(Segment) { id =>
        get {
          val maybeTx: Future[Option[Transaction]] = (txPoolActor ? GetTransaction(id)).mapTo[Option[Transaction]]
          rejectEmptyResponse { complete(maybeTx) }
        } ~
        delete {
          val txDeleted: Future[Message] = (txPoolActor ? DeleteTransaction(id)).mapTo[Message]
          onSuccess(txDeleted) { respondOnDeletion }
        } ~
        put {
          entity(as[Transaction]) { tx =>
            if (tx.id != id)
              complete((StatusCodes.InternalServerError, FailureMsg("Transaction ID in the data does not match ID on the path.")))
            else {
              val msgOnUpdate: Future[Message] = (txPoolActor ? UpdateTransaction(tx)).mapTo[Message]
              onSuccess(msgOnUpdate) { respondOnUpdate }
            }
          }
        }
      }
    }
}
