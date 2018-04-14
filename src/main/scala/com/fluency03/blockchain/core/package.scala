package com.fluency03.blockchain

import java.time.Instant

import org.json4s.NoTypeHints
import org.json4s.native.Serialization

package object core {

  implicit val formats = Serialization.formats(NoTypeHints)
  lazy val ZERO64: String = "0000000000000000000000000000000000000000000000000000000000000000"
  lazy val genesisTimestamp: Long = Instant.parse("2018-04-11T18:52:01Z").getEpochSecond

}
