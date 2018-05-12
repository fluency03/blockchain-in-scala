package com.github.fluency03.blockchain
package api.routes

import java.time.Instant

import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import com.github.fluency03.blockchain.api.{Input, SuccessMsg}

trait GenericRoutes extends RoutesSupport {
  lazy val log = Logging(system, classOf[GenericRoutes])

  lazy val genericRoutes: Route =
    pathSingleSlash {
      get {
        complete(SuccessMsg(SLOGAN))
      }
    } ~
      pathPrefix(GENERIC) {
        path(TO_SHA256) {
          post {
            entity(as[Input]) { in =>
              complete(failsafeResp(in.content.sha256))
            }
          }
        } ~
          path(TO_BASE64) {
            post {
              entity(as[Input]) { in =>
                complete(failsafeResp(in.content.toBase64))
              }
            }
          } ~
          path(FROM_BASE64) {
            post {
              entity(as[Input]) { in =>
                complete(failsafeResp(fromBase64(in.content)))
              }
            }
          } ~
          path(TO_EPOCH_TIME) {
            post {
              entity(as[Input]) { in =>
                complete(failsafeResp(epochTimeOf(in.content).toString))
              }
            }
          } ~
          path(TIME_FROM_EPOCH) {
            post {
              entity(as[Input]) { in =>
                complete(failsafeResp(Instant.ofEpochSecond(in.content.toLong).toString))
              }
            }
          }
      }

}
