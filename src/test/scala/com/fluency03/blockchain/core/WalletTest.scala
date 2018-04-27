package com.fluency03.blockchain.core

import com.fluency03.blockchain.core.Wallet._

import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable

class WalletTest extends FlatSpec with Matchers {

  "balanceOfWallet" should "obtain the balance of a Wallet based on UTXOs." in {
    val wallet = Wallet()
    val uTxOs: mutable.Map[Outpoint, TxOut] = mutable.Map.empty[Outpoint, TxOut]

    balanceOfWallet(wallet, uTxOs) shouldEqual 0
    wallet.balance(uTxOs) shouldEqual 0

    uTxOs += (Outpoint("def0", 0) -> TxOut(wallet.address, 40))
    uTxOs += (Outpoint("def0", 1) -> TxOut("abc4", 40))

    balanceOfWallet(wallet, uTxOs) shouldEqual 40
    wallet.balance(uTxOs) shouldEqual 40
  }


}
