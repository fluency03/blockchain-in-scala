package com.fluency03.blockchain
package api.routes

import java.time.Instant

import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import com.fluency03.blockchain.Util._
import com.fluency03.blockchain.api.Input

trait GenericRoutes extends RoutesSupport {
  lazy val log = Logging(system, classOf[GenericRoutes])



  lazy val genericRoutes: Route =
    pathSingleSlash {
      get {
        complete("Welcome to Blockchain in Scala!")
      }
    } ~
      pathPrefix("generic") {
        path("toSha256") {
          post {
            entity(as[Input]) { in => complete((StatusCodes.Created, in.content.toSha256)) }
          }
        } ~
        path("toBase64") {
          post {
            entity(as[Input]) { in => complete((StatusCodes.Created, in.content.toBase64)) }
          }
        } ~
        path("fromBase64") {
          post {
            entity(as[Input]) { in => complete((StatusCodes.Created, fromBase64(in.content))) }
          }
        } ~
        path("epoch-time") {
          post {
            entity(as[Input]) { in => complete((StatusCodes.Created, epochTimeOf(in.content))) }
          }
        } ~
        path("time-of-epoch") {
          post {
            entity(as[Input]) { in => complete((StatusCodes.Created, Instant.ofEpochSecond(in.content.toLong))) }
          }
        }
      }

}
