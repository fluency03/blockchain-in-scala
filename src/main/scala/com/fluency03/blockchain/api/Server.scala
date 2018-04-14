package com.fluency03.blockchain.api

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.fluency03.blockchain.api.actors.{BlockRegistryActor, BlockchainRegistryActor, TransactionRegistryActor}
import com.fluency03.blockchain.api.routes.{BlockRoutes, BlockchainRoutes}

import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.concurrent.duration.Duration

object Server extends App with BlockchainRoutes with BlockRoutes {
  implicit val system: ActorSystem = ActorSystem("blockchain-http-service")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  override lazy val log = Logging(system, classOf[App])

  val (interface, port) = (args(0), args(1).toInt)

  val blockchainRegistryActor: ActorRef = system.actorOf(BlockchainRegistryActor.props, "blockchainRegistryActor")
  val blockRegistryActor: ActorRef = system.actorOf(BlockRegistryActor.props, "blockRegistryActor")

  lazy val routes: Route = blockchainRoutes ~ blockRoutes

  val bindingFuture: Future[ServerBinding] =
    Http().bindAndHandle(routes, interface, port)

  bindingFuture.failed.foreach { ex =>
    log.error(ex, "Failed to bind to {}:{}!", interface, port)
  }

  log.info("Server online at http://{}:{}/", interface, port)

  Await.result(system.whenTerminated, Duration.Inf)
}

