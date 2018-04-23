package com.fluency03.blockchain

import java.nio.charset.Charset
import java.time.Instant

import org.json4s.NoTypeHints
import org.json4s.native.Serialization

import scala.io.Source

package object core {
  implicit val formats = Serialization.formats(NoTypeHints)

  implicit val defaultCharset: Charset = Charset.forName("UTF-8")

  val ZERO64: String = "0000000000000000000000000000000000000000000000000000000000000000"

  lazy val genesisTimestamp: Long = Instant.parse(genesisTime).getEpochSecond

  val genesisTime: String = "2018-04-11T18:52:01Z"

  val genesisMiner: String = Source.fromResource("public-key").getLines.mkString

  val SLOGAN: String = "Welcome to Blockchain in Scala!"

  class HexString(val s: String) {
    def hex: Long = java.lang.Long.parseLong(s, 16)
  }

  implicit def str2Hex(str: String): HexString = new HexString(str)

}
