package com.fluency03.blockchain
package core

import java.security.KeyPair

import com.fluency03.blockchain.Crypto
import com.fluency03.blockchain.core.Transaction._
import org.bouncycastle.util.encoders.Hex
import org.json4s.JValue
import org.json4s.native.JsonMethods.parse
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable
import scala.io.Source

class TransactionTest extends FlatSpec with Matchers {

  val genesisTx: Transaction = createCoinbaseTx(0, genesisMiner, genesisTimestamp)

  val expectedBlockJson: JValue = parse(Source.fromResource("genesis-block.json").mkString)
  val expectedGenesisBlock: Block = expectedBlockJson.extract[Block]
  val expectedHeader: BlockHeader = expectedGenesisBlock.header

  "A genesis Transaction" should "be valid." in {
    genesisTx shouldEqual expectedGenesisBlock.transactions.head

    genesisTx.txIns.length shouldEqual 1
    genesisTx.txIns.head shouldEqual TxIn(Outpoint("", 0), "")

    genesisTx.txOuts.length shouldEqual 1
    genesisTx.txOuts.head shouldEqual TxOut(genesisMiner, 50)

    val json = expectedGenesisBlock.transactions.head.toJson
    genesisTx.toJson shouldEqual json
    parse(genesisTx.toString) shouldEqual json
  }

  "A Transaction" should "be able to add TxIn(s) and keep immutability." in {
    val txIn1 = TxIn(Outpoint("", 0), "abc1")
    val txIn2 = TxIn(Outpoint("", 0), "abc2")
    val txIn3 = TxIn(Outpoint("", 0), "abc3")
    val txIn4 = TxIn(Outpoint("", 0), "abc4")
    val newTx = genesisTx.addTxIn(txIn1)

    genesisTx.txIns.length shouldEqual 1
    genesisTx.txIns.head shouldEqual TxIn(Outpoint("", 0), "")
    newTx.txIns.length shouldEqual 2
    newTx.txIns shouldEqual Seq(TxIn(Outpoint("", 0), "abc1"), TxIn(Outpoint("", 0), ""))

    val newTx2 = genesisTx.addTxIns(txIn2 +: txIn3 +: txIn4 +: Nil)
    newTx2.txIns.length shouldEqual 4
    newTx2.txIns shouldEqual Seq(
      TxIn(Outpoint("", 0), "abc2"),
      TxIn(Outpoint("", 0), "abc3"),
      TxIn(Outpoint("", 0), "abc4"),
      TxIn(Outpoint("", 0), ""))

    val newTx3 = newTx2.removeTxIn(txIn2)
    newTx3.txIns.length shouldEqual 3
    newTx3.txIns shouldEqual Seq(
      TxIn(Outpoint("", 0), "abc3"),
      TxIn(Outpoint("", 0), "abc4"),
      TxIn(Outpoint("", 0), ""))
  }

  "A Transaction" should "be able to add TxOut(s) and keep immutability." in {
    val txOut1 = TxOut("abc1", 10)
    val txOut2 = TxOut("abc2", 20)
    val txOut3 = TxOut("abc3", 30)
    val txOut4 = TxOut("abc4", 40)
    val newTx = genesisTx.addTxOut(txOut1)

    genesisTx.txOuts.length shouldEqual 1
    genesisTx.txOuts.head shouldEqual TxOut(genesisMiner, 50)
    newTx.txOuts.length shouldEqual 2
    newTx.txOuts shouldEqual Seq(TxOut("abc1", 10), TxOut(genesisMiner, COINBASE_AMOUNT))

    val newTx2 = genesisTx.addTxOuts(txOut2 +: txOut3 +: txOut4 +: Nil)

    newTx2.txOuts.length shouldEqual 4
    newTx2.txOuts shouldEqual Seq(
      TxOut("abc2", 20),
      TxOut("abc3", 30),
      TxOut("abc4", 40),
      TxOut(genesisMiner, COINBASE_AMOUNT))

    val newTx3 = newTx2.removeTxOut(txOut3)
    newTx3.txOuts.length shouldEqual 3
    newTx3.txOuts shouldEqual Seq(
      TxOut("abc2", 20),
      TxOut("abc4", 40),
      TxOut(genesisMiner, COINBASE_AMOUNT))
  }

  "Transaction" should "be able to be signed by key pair." in {
    val txIn = TxIn(Outpoint("def0", 0), "abc")
    val pair: KeyPair = Crypto.generateKeyPair()
    val hash = "ace0".hex2Bytes

    val signature = Crypto.sign(hash, pair.getPrivate.getEncoded)
    Crypto.verify(hash, pair.getPublic.getEncoded, signature) shouldEqual true

    val unspentTxOuts: mutable.Map[Outpoint, TxOut] = mutable.Map.empty[Outpoint, TxOut]
    val signedTxIn0 = signTxIn(hash, txIn, pair, unspentTxOuts)
    signedTxIn0 shouldEqual None

    unspentTxOuts += (Outpoint("def0", 0) -> TxOut(pair.getPublic.getEncoded.toHex, 40))
    unspentTxOuts += (Outpoint("def0", 1) -> TxOut("abc4", 40))

    val signedTxIn = signTxIn(hash, txIn, pair, unspentTxOuts)
    signedTxIn shouldEqual Some(TxIn(Outpoint("def0", 0), signedTxIn.get.signature))

    signedTxIn.get.previousOut shouldEqual Outpoint("def0", 0)
    Crypto.verify(hash, pair.getPublic.getEncoded, signedTxIn.get.signature.hex2Bytes) shouldEqual true
  }

  "Transaction" should "have valid TxIns." in {
    val pair: KeyPair = Crypto.generateKeyPair()
    val hash = "ace0".hex2Bytes

    val signature = Crypto.sign(hash, pair.getPrivate.getEncoded)
    Crypto.verify(hash, pair.getPublic.getEncoded, signature) shouldEqual true

    val txIn = TxIn(Outpoint("def0", 0), "abc1")
    val unspentTxOuts: mutable.Map[Outpoint, TxOut] = mutable.Map.empty[Outpoint, TxOut]
    unspentTxOuts += (Outpoint("def0", 0) -> TxOut(pair.getPublic.getEncoded.toHex, 40))
    unspentTxOuts += (Outpoint("def0", 1) -> TxOut("abc4", 40))

    val signedTxIn = signTxIn(hash, txIn, pair, unspentTxOuts)

    val unspentTxOuts0: mutable.Map[Outpoint, TxOut] = mutable.Map.empty[Outpoint, TxOut]

    validateTxIn(signedTxIn.get, hash, unspentTxOuts0) shouldEqual false
    validateTxIn(signedTxIn.get, hash, unspentTxOuts) shouldEqual true
  }

  "Transaction" should "have valid TxOut values." in {
    val pair: KeyPair = Crypto.generateKeyPair()

    val unspentTxOuts: mutable.Map[Outpoint, TxOut] = mutable.Map.empty[Outpoint, TxOut]
    unspentTxOuts += (Outpoint("def0", 1) -> TxOut("abc4", 20))

    val tx: Transaction = Transaction(
      Seq(TxIn(Outpoint("def0", 0), "abc1"), TxIn(Outpoint("def0", 1), "abc1")),
      Seq(TxOut("abc4", 40)),
      genesisTimestamp
    )

    validateTxOutValues(tx, unspentTxOuts) shouldEqual false

    unspentTxOuts += (Outpoint("def0", 0) -> TxOut("abc1", 20))
    validateTxOutValues(tx, unspentTxOuts) shouldEqual true
  }

  "Transaction" should "have be validatable." in {
    // validateTransaction
  }



}
