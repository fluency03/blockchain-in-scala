package com.github.fluency03.blockchain.core

import com.github.fluency03.blockchain.core.KeyContainer.balanceOfKey

import scala.collection.mutable

trait Wallet {

  def size(): Int

  def getKey(str: String): Option[KeyContainer]

  def newKey(): KeyContainer

  def balance(uTxOs: mutable.Map[Outpoint, TxOut]): Long

}


case class RandomWallet() extends Wallet {

  val keys: mutable.Map[String, KeyContainer] = {
    val initKeys = mutable.Map.empty[String, KeyContainer]
    val aNewKey = KeyContainer()
    initKeys += (aNewKey.publicKeyHex -> aNewKey)
    initKeys
  }

  override def getKey(hash: String): Option[KeyContainer] = keys.get(hash)

  override def newKey(): KeyContainer = {
    val aNewKey = KeyContainer()
    keys += (aNewKey.publicKeyHex -> aNewKey)
    aNewKey
  }

  override def balance(uTxOs: mutable.Map[Outpoint, TxOut]): Long =
    keys.values.map(k => balanceOfKey(k, uTxOs)).sum

  override def size(): Int = keys.size

}


// TODO (Chang): To be implemented
case class SeededWallet() extends Wallet {

  override def getKey(str: String): Option[KeyContainer] = ???

  override def newKey(): KeyContainer = ???

  override def balance(uTxOs: mutable.Map[Outpoint, TxOut]): Long = ???

  override def size(): Int = ???

}
