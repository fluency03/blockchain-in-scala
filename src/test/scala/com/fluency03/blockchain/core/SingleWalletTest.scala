package com.fluency03.blockchain
package core

import com.fluency03.blockchain.core.SingleWallet._

import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable

class SingleWalletTest extends FlatSpec with Matchers {

  "balanceOfWallet" should "obtain the balance of a Wallet based on UTXOs." in {
    val wallet = SingleWallet()
    val uTxOs: mutable.Map[Outpoint, TxOut] = mutable.Map.empty[Outpoint, TxOut]

    balanceOfWallet(wallet, uTxOs) shouldEqual 0
    wallet.balance(uTxOs) shouldEqual 0

    uTxOs += (Outpoint("def0", 0) -> TxOut(wallet.address, 40))
    uTxOs += (Outpoint("def0", 1) -> TxOut("abc4", 40))

    balanceOfWallet(wallet, uTxOs) shouldEqual 40
    wallet.balance(uTxOs) shouldEqual 40
  }

  "Wallet" should "be able to sign a TxIn." in {
    val wallet = SingleWallet()
    val id = "".toSha256
    val txIn = TxIn(Outpoint("def0", 0), "abc")
    val uTxOs: mutable.Map[Outpoint, TxOut] = mutable.Map.empty[Outpoint, TxOut]

    val signedTxIn0 = wallet.sign(id, txIn, uTxOs)
    signedTxIn0 shouldEqual None

    uTxOs += (Outpoint("def0", 0) -> TxOut(wallet.address, 40))
    uTxOs += (Outpoint("def0", 1) -> TxOut("abc4", 40))

    val signedTxIn = wallet.sign(id, txIn, uTxOs)
    signedTxIn shouldEqual Some(TxIn(Outpoint("def0", 0), signedTxIn.get.signature))
  }


}
