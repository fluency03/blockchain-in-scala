package com.github.fluency03.blockchain.core

import com.github.fluency03.blockchain.core.KeyContainer.balanceOfKey

import scala.collection.mutable

trait Wallet {

  def getKey(str: String): Option[KeyContainer]

  def newKey(): KeyContainer

  def balance(uTxOs: mutable.Map[Outpoint, TxOut]): Long

}


case class RandomWallet() extends Wallet {

  private[this] val keys = mutable.Map.empty[String, KeyContainer]

  override def getKey(str: String): Option[KeyContainer] = keys.get(str)

  override def newKey(): KeyContainer = {
    val aNewKey = KeyContainer()
    keys += (aNewKey.publicKeyHex -> aNewKey)
    aNewKey
  }

  override def balance(uTxOs: mutable.Map[Outpoint, TxOut]): Long =
    keys.values.map(k => balanceOfKey(k, uTxOs)).sum


}


// TODO (Chang): To be implemented
case class SeededWallet() extends Wallet {

  override def getKey(str: String): Option[KeyContainer] = ???

  override def newKey(): KeyContainer = ???

  override def balance(uTxOs: mutable.Map[Outpoint, TxOut]): Long = ???

}
