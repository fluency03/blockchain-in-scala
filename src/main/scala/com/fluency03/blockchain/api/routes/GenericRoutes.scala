package com.fluency03.blockchain.api.routes

import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import com.fluency03.blockchain.Util._

case class Input(data: String)

trait GenericRoutes extends Routes {
  lazy val log = Logging(system, classOf[GenericRoutes])

  lazy val genericRoutes: Route =
    pathPrefix("generic") {
      path("hash-of-string") {
        post {
          entity(as[Input]) { in => complete((StatusCodes.Created, hashOf(in.data))) }
        }
      } ~
      path("base64-of-string") {
        post {
          entity(as[Input]) { in => complete((StatusCodes.Created, toBase64(in.data))) }
        }
      } ~
      path("string-of-base64") {
        post {
          entity(as[Input]) { in => complete((StatusCodes.Created, fromBase64(in.data))) }
        }
      }
    }
}
