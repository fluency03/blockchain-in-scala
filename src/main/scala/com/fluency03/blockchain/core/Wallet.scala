package com.fluency03.blockchain
package core

import java.security.KeyPair

import com.fluency03.blockchain.core.Wallet.balanceOfWallet
import com.fluency03.blockchain.core.Transaction.signTxIn

import scala.collection.mutable

case class Wallet() {

  private[this] val keyPair: KeyPair = Crypto.generateKeyPair()

  lazy val address: String = keyPair.getPublic.toHex

  def balance(uTxOs: mutable.Map[Outpoint, TxOut]): Long = balanceOfWallet(this, uTxOs)

  def sign(txId: String, txIn: TxIn, uTxOs: mutable.Map[Outpoint, TxOut]): Option[TxIn] =
    signTxIn(txId, txIn, keyPair, uTxOs)

}

object Wallet {

  def balanceOfWallet(wallet: Wallet, uTxOs: mutable.Map[Outpoint, TxOut]): Long =
    balanceOfAddress(wallet.address, uTxOs)

  def balanceOfAddress(address: String, uTxOs: mutable.Map[Outpoint, TxOut]): Long =
    uTxOs.values.filter(_.address == address).map(_.amount).sum

}