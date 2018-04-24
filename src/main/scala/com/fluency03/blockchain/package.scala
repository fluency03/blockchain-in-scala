package com.fluency03

import java.nio.charset.Charset
import java.time.Instant

import com.fluency03.blockchain.Util.{sha256HashOf, base64Of, binaryOfHex}
import org.bouncycastle.util.encoders.{Base64, Hex}
import org.json4s.{Formats, NoTypeHints}
import org.json4s.native.Serialization

import scala.io.Source

package object blockchain {

  type Bytes = Array[Byte]

  implicit val formats: AnyRef with Formats = Serialization.formats(NoTypeHints)

  implicit val defaultCharset: Charset = Charset.forName("UTF-8")

  val ZERO64: String = "0000000000000000000000000000000000000000000000000000000000000000"

  lazy val genesisTimestamp: Long = Instant.parse(genesisTime).getEpochSecond

  val genesisTime: String = "2018-04-11T18:52:01Z"

  val genesisMiner: String = Source.fromResource("public-key").getLines.mkString

  val SLOGAN: String = "Welcome to Blockchain in Scala!"

  implicit class StringImplicit(val str: String) {
    def hex2Long: Long = java.lang.Long.parseLong(str, 16)
    def hex2Bytes: Array[Byte] = Hex.decode(str)
    def hex2Binary: String = binaryOfHex(str)
    def toBase64: String = base64Of(str.getBytes("UTF-8"))
    def toSha256: String = sha256HashOf(str)
  }

  implicit class BytesImplicit(val bytes: Bytes) {
    def toHex: String = Hex.toHexString(bytes)
    def toBase64: String = base64Of(bytes)
  }

}
