package com.github.fluency03.blockchain
package core

import java.security.KeyPair

import com.github.fluency03.blockchain.core.SingleWallet.balanceOfWallet
import com.github.fluency03.blockchain.core.Transaction.signTxIn

import scala.collection.mutable

case class SingleWallet() {

  private[this] val keyPair: KeyPair = Crypto.generateKeyPair()

  lazy val address: String = keyPair.getPublic.toHex

  def balance(uTxOs: mutable.Map[Outpoint, TxOut]): Long = balanceOfWallet(this, uTxOs)

  def sign(txId: String, txIn: TxIn, uTxOs: mutable.Map[Outpoint, TxOut]): Option[TxIn] =
    signTxIn(txId, txIn, keyPair, uTxOs)

}

object SingleWallet {

  def balanceOfWallet(wallet: SingleWallet, uTxOs: mutable.Map[Outpoint, TxOut]): Long =
    balanceOfAddress(wallet.address, uTxOs)

  def balanceOfAddress(address: String, uTxOs: mutable.Map[Outpoint, TxOut]): Long =
    uTxOs.values.filter(_.address == address).map(_.amount).sum

}