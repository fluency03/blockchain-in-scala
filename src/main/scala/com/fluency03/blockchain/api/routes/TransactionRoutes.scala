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
import com.fluency03.blockchain.api.actors.TransactionsActor._
import com.fluency03.blockchain.api.{Message, Transactions}
import com.fluency03.blockchain.core.Transaction

import scala.concurrent.Future

trait TransactionRoutes extends RoutesSupport {
  lazy val log = Logging(system, classOf[TransactionRoutes])

  def transActor: ActorRef

  /**
   * TODO (Chang):
   *  - Update transaction
   *  - Sign transaction
   *
   */

  lazy val transRoutes: Route =
    path(TRANSACTIONS) {
      parameters( 'ids.as(CsvSeq[String]) ? ) { ids =>
        val transactions: Future[Transactions] =
          if (ids.isDefined) (transActor ? GetTransactions(ids.get.toSet)).mapTo[Transactions]
          else (transActor ? GetTransactions).mapTo[Transactions]
        complete(transactions)
      }
    } ~
    pathPrefix(TRANSACTION) {
      pathEnd {
        post {
          entity(as[Transaction]) { tx =>
            val txCreated: Future[Message] = (transActor ? CreateTransaction(tx)).mapTo[Message]
            onSuccess(txCreated) { respondOnCreation }
          }
        }
      } ~
      path(Segment) { id =>
        get {
          val maybeTx: Future[Option[Transaction]] = (transActor ? GetTransaction(id)).mapTo[Option[Transaction]]
          rejectEmptyResponse { complete(maybeTx) }
        } ~
        delete {
          val txDeleted: Future[Message] = (transActor ? DeleteTransaction(id)).mapTo[Message]
          onSuccess(txDeleted) { respondOnDeletion }
        }
      }
    }
}
