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
import com.fluency03.blockchain.api.actors.TransactionActor._
import com.fluency03.blockchain.api.utils.GenericMessage.Response
import com.fluency03.blockchain.core.Transaction

import scala.concurrent.Future

trait TransactionRoutes extends Routes {
  lazy val log = Logging(system, classOf[TransactionRoutes])

  def txActor: ActorRef

  lazy val txRoutes: Route =
    pathPrefix("transactions") {
      pathEnd {
        get {
          val transactions: Future[Transactions] = (txActor ? GetTransactions).mapTo[Transactions]
          complete(transactions)
        } ~
        post {
          entity(as[Transaction]) { tx =>
            val txCreated: Future[Response] = (txActor ? CreateTransaction(tx)).mapTo[Response]
            onSuccess(txCreated) { resp =>
              log.info("Created transaction [{}]: {}", tx.hash, resp.message)
              complete((StatusCodes.Created, resp))
            }
          }
        }
      } ~
      path(Segment) { hash =>
        get {
          val maybeTx: Future[Option[Transaction]] = (txActor ? GetTransaction(hash)).mapTo[Option[Transaction]]
          rejectEmptyResponse { complete(maybeTx) }
        } ~
        delete {
          val txDeleted: Future[Response] = (txActor ? DeleteTransaction(hash)).mapTo[Response]
          onSuccess(txDeleted) { resp =>
            log.info("Deleted transaction [{}]: {}", hash, resp.message)
            complete((StatusCodes.OK, resp))
          }
        }
      }
    }
}
