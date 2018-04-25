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
import com.fluency03.blockchain.api.{FailureMsg, Message, SuccessMsg, Transactions}
import com.fluency03.blockchain.api.actors.TransactionsActor._
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
            val txCreated: Future[Message] = (transActor ? CreateTransaction(tx)).mapTo[Message]
            onSuccess(txCreated) {
              case SuccessMsg(content) => complete((StatusCodes.Created, content))
              case FailureMsg(content) => complete((StatusCodes.Conflict, content))
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
          val txDeleted: Future[Message] = (transActor ? DeleteTransaction(id)).mapTo[Message]
          onSuccess(txDeleted) {
            case SuccessMsg(content) => complete((StatusCodes.OK, content))
            case FailureMsg(content) => complete((StatusCodes.NotFound, content))
          }
        }
      }
    }
}
