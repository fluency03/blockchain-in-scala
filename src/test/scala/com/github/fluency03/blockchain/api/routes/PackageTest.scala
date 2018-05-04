package com.github.fluency03.blockchain.api.routes

import akka.http.scaladsl.model.StatusCodes
import com.github.fluency03.blockchain.api.{FailureMsg, SuccessMsg}
import org.scalatest.{FlatSpec, Matchers}

class PackageTest extends FlatSpec with Matchers {

  "failsafeMsg" should "return corresponding message based on success/failure of the function." in {
    failsafeMsg({
      throw new Exception("some error")
      "some string"
    }) shouldEqual FailureMsg("some error")

    failsafeMsg({
      "some string"
    }) shouldEqual SuccessMsg("some string")

  }

  "failsafeResp" should "return corresponding message based on success/failure of the function." in {
    failsafeResp({
      throw new Exception("some error")
      "some string"
    }) shouldEqual (StatusCodes.InternalServerError, FailureMsg("some error"))

    failsafeResp({
      "some string"
    }) shouldEqual (StatusCodes.OK, SuccessMsg("some string"))

  }

}
