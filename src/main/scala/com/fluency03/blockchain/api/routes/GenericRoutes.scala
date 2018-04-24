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
import com.fluency03.blockchain.api.utils.GenericMessage.Input

trait GenericRoutes extends Routes {
  lazy val log = Logging(system, classOf[GenericRoutes])

  lazy val genericRoutes: Route =
    pathSingleSlash {
      get {
        complete("Welcome to Blockchain in Scala!")
      }
    } ~
      pathPrefix("generic") {
        path("hash-of-string") {
          post {
            entity(as[Input]) { in => complete((StatusCodes.Created, in.data.toSha256)) }
          }
        } ~
        path("base64-of-string") {
          post {
            entity(as[Input]) { in => complete((StatusCodes.Created, in.data.toBase64)) }
          }
        } ~
        path("string-of-base64") {
          post {
            entity(as[Input]) { in => complete((StatusCodes.Created, fromBase64(in.data))) }
          }
        } ~
        path("epoch-time") {
          post {
            entity(as[Input]) { in => complete((StatusCodes.Created, epochTimeOf(in.data))) }
          }
        } ~
        path("time-from-epoch") {
          post {
            entity(as[Input]) { in => complete((StatusCodes.Created, Instant.ofEpochSecond(in.data.toLong))) }
          }
        }
      }

}
