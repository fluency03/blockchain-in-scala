package com.fluency03.blockchain.api

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.fluency03.blockchain.api.actors._
import com.fluency03.blockchain.api.routes._
import com.typesafe.config.ConfigFactory

import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.concurrent.duration.Duration

object Server extends App
    with BlockchainRoutes with BlockRoutes with TransactionRoutes with NetworkRoutes with GenericRoutes {
  // we leave these abstract, since they will be provided by the App
  implicit val system: ActorSystem = ActorSystem("blockchain-http-service")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  override lazy val log = Logging(system, classOf[App])

  val config = ConfigFactory.load()
  val httpConfig = config.getConfig("http")
  val (host, port) = (httpConfig.getString("host"), httpConfig.getInt("port"))

  val blockchainActor: ActorRef = system.actorOf(BlockchainActor.props, BLOCKCHAIN_ACTOR_NAME)
  val blocksActor: ActorRef = system.actorOf(BlocksActor.props, BLOCKS_ACTOR_NAME)
  val transActor: ActorRef = system.actorOf(TransactionsActor.props, TRANS_ACTOR_NAME)
  val networkActor: ActorRef = system.actorOf(NetworkActor.props, NETWORK_ACTOR_NAME)

  lazy val routes: Route = blockchainRoutes ~ blockRoutes ~ transRoutes ~ networkRoutes ~ genericRoutes

  val bindingFuture: Future[ServerBinding] =
    Http().bindAndHandle(routes, host, port)

  bindingFuture.failed.foreach { ex =>
    log.error(ex, "Failed to bind to {}:{}!", host, port)
  }

  log.info("Server online at http://{}:{}/", host, port)

  Await.result(system.whenTerminated, Duration.Inf)
}

