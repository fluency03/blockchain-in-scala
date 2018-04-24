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
import com.fluency03.blockchain.api.Transactions
import com.fluency03.blockchain.api.actors.TransactionsActor._
import com.fluency03.blockchain.api.utils.GenericMessage.Response
import com.fluency03.blockchain.core.Transaction

import scala.concurrent.Future

trait TransactionRoutes extends Routes {
  lazy val log = Logging(system, classOf[TransactionRoutes])

  def transActor: ActorRef

  lazy val transRoutes: Route =
    path("transactions") {
      get {
        val transactions: Future[Transactions] = (transActor ? GetTransactions).mapTo[Transactions]
        complete(transactions)
      }
    } ~
    pathPrefix("transaction") {
      pathEnd {
        post {
          entity(as[Transaction]) { tx =>
            val txCreated: Future[Response] = (transActor ? CreateTransaction(tx)).mapTo[Response]
            onSuccess(txCreated) { resp =>
              log.info("Created transaction [{}]: {}", tx.id, resp.message)
              complete((StatusCodes.Created, resp))
            }
          }
        }
      } ~
      path(Segment) { id =>
        get {
          val maybeTx: Future[Option[Transaction]] = (transActor ? GetTransaction(id)).mapTo[Option[Transaction]]
          rejectEmptyResponse { complete(maybeTx) }
        } ~
        delete {
          val txDeleted: Future[Response] = (transActor ? DeleteTransaction(id)).mapTo[Response]
          onSuccess(txDeleted) { resp =>
            log.info("Deleted transaction [{}]: {}", id, resp.message)
            complete((StatusCodes.OK, resp))
          }
        }
      }
    }
}
