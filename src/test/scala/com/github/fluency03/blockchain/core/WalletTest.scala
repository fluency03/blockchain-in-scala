package com.github.fluency03.blockchain.core

import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable

class WalletTest extends FlatSpec with Matchers {

  "RandomWallet" should "maintain a set of keys in random way." in {
    val wallet = RandomWallet()
    wallet shouldBe a[Wallet]
    wallet.size() shouldEqual 1
    val kc1 = wallet.keys.values.head

    wallet.getKey(kc1.publicKeyHex) shouldEqual Some(kc1)
    wallet.getKey("") shouldEqual None

    val kc2 = wallet.newKey()
    kc2 shouldBe a[KeyContainer]
    wallet.size() shouldEqual 2
    wallet.getKey(kc2.publicKeyHex) shouldEqual Some(kc2)

    val uTxOs: mutable.Map[Outpoint, TxOut] = mutable.Map.empty[Outpoint, TxOut]
    wallet.balance(uTxOs) shouldEqual 0

    uTxOs += (Outpoint("def0", 0) -> TxOut(kc1.publicKeyHex, 40))
    uTxOs += (Outpoint("def0", 1) -> TxOut("abc4", 40))
    wallet.balance(uTxOs) shouldEqual 40
    kc1.balance(uTxOs) shouldEqual 40
    kc2.balance(uTxOs) shouldEqual 0

    uTxOs += (Outpoint("def0", 2) -> TxOut(kc2.publicKeyHex, 40))
    wallet.balance(uTxOs) shouldEqual 80

    kc1.balance(uTxOs) shouldEqual 40
    kc2.balance(uTxOs) shouldEqual 40
  }

  "SeededWallet" should "maintain a set of keys in seeded way." in {
    val wallet = SeededWallet()

    // TODO (Chang): tests after methods being implemented
    a[NotImplementedError] should be thrownBy wallet.size()
    a[NotImplementedError] should be thrownBy wallet.newKey()
    a[NotImplementedError] should be thrownBy wallet.getKey("")
    a[NotImplementedError] should be thrownBy wallet.balance(mutable.Map.empty[Outpoint, TxOut])

  }

}
