package com.github.fluency03.blockchain
package core

import com.github.fluency03.blockchain.core.KeyContainer._

import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable

class KeyContainerTest extends FlatSpec with Matchers {

  "balanceOfKey" should "obtain the balance of a Key based on UTXOs." in {
    val kc = KeyContainer()
    val uTxOs: mutable.Map[Outpoint, TxOut] = mutable.Map.empty[Outpoint, TxOut]

    balanceOfKey(kc, uTxOs) shouldEqual 0
    kc.balance(uTxOs) shouldEqual 0

    uTxOs += (Outpoint("def0", 0) -> TxOut(kc.address, 40))
    uTxOs += (Outpoint("def0", 1) -> TxOut("abc4", 40))

    balanceOfKey(kc, uTxOs) shouldEqual 40
    kc.balance(uTxOs) shouldEqual 40
  }

  "KeyContainer" should "be able to sign a TxIn." in {
    val kc = KeyContainer()
    val id = "".toSha256
    val txIn = TxIn(Outpoint("def0", 0), "abc")
    val uTxOs: mutable.Map[Outpoint, TxOut] = mutable.Map.empty[Outpoint, TxOut]

    val signedTxIn0 = kc.sign(id, txIn, uTxOs)
    signedTxIn0 shouldEqual None

    uTxOs += (Outpoint("def0", 0) -> TxOut(kc.address, 40))
    uTxOs += (Outpoint("def0", 1) -> TxOut("abc4", 40))

    val signedTxIn = kc.sign(id, txIn, uTxOs)
    signedTxIn shouldEqual Some(TxIn(Outpoint("def0", 0), signedTxIn.get.signature))
  }


}
