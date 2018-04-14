package com.fluency03.blockchain.api

import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.native.Serialization
import org.json4s.{DefaultFormats, Formats, Serialization}

trait JsonSupport extends Json4sSupport {

  implicit val formats: Formats = DefaultFormats
  implicit val naiveSerialization: Serialization = Serialization

}

object JsonSupport extends JsonSupport