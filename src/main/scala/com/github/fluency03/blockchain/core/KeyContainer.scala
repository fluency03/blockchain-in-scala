package com.github.fluency03.blockchain
package core

import java.security.KeyPair

import com.github.fluency03.blockchain.core.KeyContainer.balanceOfKey
import com.github.fluency03.blockchain.core.Transaction.signTxIn
import com.github.fluency03.blockchain.crypto.Secp256k1

import scala.collection.mutable

case class KeyContainer() {

  private[this] val keyPair: KeyPair = Secp256k1.generateKeyPair()

  // TODO (Chang): change it to actual address of a PublicKey
  lazy val address: String = keyPair.getPublic.toHex

  lazy val publicKeyHex: String = keyPair.getPublic.toHex

  def balance(uTxOs: mutable.Map[Outpoint, TxOut]): Long = balanceOfKey(this, uTxOs)

  def sign(txId: String, txIn: TxIn, uTxOs: mutable.Map[Outpoint, TxOut]): Option[TxIn] =
    signTxIn(txId, txIn, keyPair, uTxOs)

}

object KeyContainer {

  def balanceOfKey(kc: KeyContainer, uTxOs: mutable.Map[Outpoint, TxOut]): Long =
    balanceOfAddress(kc.address, uTxOs)

  def balanceOfAddress(address: String, uTxOs: mutable.Map[Outpoint, TxOut]): Long =
    uTxOs.values.filter(_.address == address).map(_.amount).sum

}