package com.github.fluency03.blockchain.core

import org.scalatest.{FlatSpec, Matchers}

class PeerTest extends FlatSpec with Matchers {

  "A Peer" should "contain valid name and Set of public keys." in {
    val p = Peer("peer", Set("abcd"))
    p.name shouldEqual "peer"
    p.publicKeys shouldEqual Set("abcd")
    p shouldBe a[Peer]
    p should not be a[PeerSimple]
    val ps = p.toSimple
    ps shouldBe a[PeerSimple]
    ps.name shouldEqual "peer"
  }

}
