package com.fluency03.blockchain.api

package object routes {

  // generics
  val SLASH = "/"
  val GENERIC = "generic"
  val TO_SHA256 = "to-sha256"
  val TO_BASE64 = "to-base64"
  val FROM_BASE64 = "from-base64"
  val TO_EPOCH_TIME = "to-epoch-time"
  val TIME_FROM_EPOCH = "time-from-epoch"

  def pathOf(seg: String*): String = SLASH + seg.mkString(SLASH)

}
