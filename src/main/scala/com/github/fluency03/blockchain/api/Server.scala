package com.github.fluency03.blockchain.api

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.github.fluency03.blockchain.api.actors._
import com.github.fluency03.blockchain.api.routes._
import com.typesafe.config.ConfigFactory

import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.concurrent.duration.Duration

object Server
  extends App
  with BlockchainRoutes
  with BlockPoolRoutes
  with TxPoolRoutes
  with NetworkRoutes
  with GenericRoutes {

  // we leave these abstract, since they will be provided by the App
  implicit val system: ActorSystem = ActorSystem("blockchain-http-service")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  override lazy val log = Logging(system, classOf[App])

  val config = ConfigFactory.load()
  val httpConfig = config.getConfig("http")
  val (host, port) = (httpConfig.getString("host"), httpConfig.getInt("port"))

  val blockchainActor: ActorRef = system.actorOf(BlockchainActor.props, BLOCKCHAIN_ACTOR_NAME)
  val blockPoolActor: ActorRef = system.actorOf(BlockPoolActor.props, BLOCK_POOL_ACTOR_NAME)
  val txPoolActor: ActorRef = system.actorOf(TxPoolActor.props, TX_POOL_ACTOR_NAME)
  val networkActor: ActorRef = system.actorOf(NetworkActor.props, NETWORK_ACTOR_NAME)

  lazy val routes: Route =
    blockchainRoutes ~ blockPoolRoutes ~ txPoolRoutes ~ networkRoutes ~ genericRoutes

  val bindingFuture: Future[ServerBinding] = Http().bindAndHandle(routes, host, port)

  bindingFuture.failed.foreach { ex =>
    log.error(ex, "Failed to bind to {}:{}!", host, port)
  }

  log.info("Server online at http://{}:{}/", host, port)

  Await.result(system.whenTerminated, Duration.Inf)
}

