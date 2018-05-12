package com.github.fluency03.blockchain
package core

import java.security._

import com.github.fluency03.blockchain.core.KeyContainer.balanceOfKey
import com.github.fluency03.blockchain.core.Transaction.signTxIn
import com.github.fluency03.blockchain.crypto.Secp256k1

import scala.collection.mutable

case class KeyContainer() {

  val keyPair: KeyPair = Secp256k1.generateKeyPair()

  // TODO (Chang): change it to actual address (which is a Base58) of a PublicKey
  lazy val address: HexString = keyPair.getPublic.toHex

  lazy val publicKeyHex: HexString = keyPair.getPublic.toHex

  def balance(uTxOs: mutable.Map[Outpoint, TxOut]): Long = balanceOfKey(this, uTxOs)

  def sign(data: Bytes): Bytes = Secp256k1.sign(data, keyPair.getPrivate)

  def verify(data: Bytes, signature: Bytes): Boolean =
    Secp256k1.verify(data, keyPair.getPublic, signature)

  def sign(txId: String, txIn: TxIn, uTxOs: mutable.Map[Outpoint, TxOut]): Option[TxIn] =
    signTxIn(txId, txIn, keyPair, uTxOs)

}

object KeyContainer {

  def balanceOfKey(kc: KeyContainer, uTxOs: mutable.Map[Outpoint, TxOut]): Long =
    balanceOfAddress(kc.address, uTxOs)

  def balanceOfAddress(address: String, uTxOs: mutable.Map[Outpoint, TxOut]): Long =
    uTxOs.values.filter(_.address == address).map(_.amount).sum

}