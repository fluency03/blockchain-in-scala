package com.fluency03.blockchain.api.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.fluency03.blockchain.api.{Input, JsonSupport, SuccessMsg}
import org.scalatest.{Matchers, WordSpec}
import com.fluency03.blockchain.api.Server.genericRoutes

class GenericRoutesTest extends WordSpec with Matchers with ScalatestRouteTest with JsonSupport {

  "GenericRoutes" should {
    "return a greeting for GET requests to the root path." in {
      Get() ~> genericRoutes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[SuccessMsg] shouldEqual SuccessMsg("Welcome to Blockchain in Scala!")
      }
    }

    "return corresponding value." in {
      Post(pathOf(GENERIC, TO_SHA256), Input("open sesame")) ~> genericRoutes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[SuccessMsg] shouldEqual SuccessMsg("41ef4bb0b23661e66301aac36066912dac037827b4ae63a7b1165a5aa93ed4eb")
      }

      Post(pathOf(GENERIC, TO_SHA256), Input("")) ~> genericRoutes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[SuccessMsg] shouldEqual SuccessMsg("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
      }

      Post(pathOf(GENERIC, TO_BASE64), Input("open sesame")) ~> genericRoutes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[SuccessMsg] shouldEqual SuccessMsg("b3BlbiBzZXNhbWU=")
      }

      Post(pathOf(GENERIC, FROM_BASE64), Input("b3BlbiBzZXNhbWU=")) ~> genericRoutes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[SuccessMsg] shouldEqual SuccessMsg("open sesame")
      }

      Post(pathOf(GENERIC, TO_EPOCH_TIME), Input("2018-04-11T18:52:01Z")) ~> genericRoutes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[SuccessMsg] shouldEqual SuccessMsg("1523472721")
      }

      Post(pathOf(GENERIC, TIME_FROM_EPOCH), Input("1523472721")) ~> genericRoutes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[SuccessMsg] shouldEqual SuccessMsg("2018-04-11T18:52:01Z")
      }
    }
  }

}
