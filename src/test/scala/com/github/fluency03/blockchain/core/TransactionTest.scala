package com.github.fluency03.blockchain
package core

import java.security.KeyPair

import com.github.fluency03.blockchain.core.Transaction._
import com.github.fluency03.blockchain.crypto.Secp256k1
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

    validateCoinbaseTx(genesisTx, 0)
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
    val pair: KeyPair = Secp256k1.generateKeyPair()
    val hash = "ace0"

    val signature = Secp256k1.sign(hash.hex2Bytes, pair.getPrivate.getEncoded)
    Secp256k1.verify(hash.hex2Bytes, pair.getPublic.getEncoded, signature) shouldEqual true

    val uTxOs: mutable.Map[Outpoint, TxOut] = mutable.Map.empty[Outpoint, TxOut]
    val signedTxIn0 = signTxIn(hash, txIn, pair, uTxOs)
    signedTxIn0 shouldEqual None

    uTxOs += (Outpoint("def0", 0) -> TxOut("0000", 40))
    val signedTxIn1 = signTxIn(hash, txIn, pair, uTxOs)
    signedTxIn1 shouldEqual None

    uTxOs += (Outpoint("def0", 0) -> TxOut(pair.getPublic.toHex, 40))
    uTxOs += (Outpoint("def0", 1) -> TxOut("abc4", 40))

    val signedTxIn = signTxIn(hash, txIn, pair, uTxOs)
    signedTxIn shouldEqual Some(TxIn(Outpoint("def0", 0), signedTxIn.get.signature))

    signedTxIn.get.previousOut shouldEqual Outpoint("def0", 0)
    Secp256k1.verify(hash.hex2Bytes, pair.getPublic.getEncoded, signedTxIn.get.signature.hex2Bytes) shouldEqual true
  }

  "Transaction" should "have valid TxIns." in {
    val pair: KeyPair = Secp256k1.generateKeyPair()
    val hash = "ace0"

    val signature = Secp256k1.sign(hash.hex2Bytes, pair.getPrivate.getEncoded)
    Secp256k1.verify(hash.hex2Bytes, pair.getPublic.getEncoded, signature) shouldEqual true

    val txIn = TxIn(Outpoint("def0", 0), "abc1")
    val uTxOs: mutable.Map[Outpoint, TxOut] = mutable.Map.empty[Outpoint, TxOut]
    uTxOs += (Outpoint("def0", 0) -> TxOut(pair.getPublic.toHex, 40))
    uTxOs += (Outpoint("def0", 1) -> TxOut("abc4", 40))

    val signedTxIn = signTxIn(hash, txIn, pair, uTxOs)

    val uTxOs0: mutable.Map[Outpoint, TxOut] = mutable.Map.empty[Outpoint, TxOut]

    validateTxIn(signedTxIn.get, hash, uTxOs0) shouldEqual false
    validateTxIn(signedTxIn.get, hash, uTxOs) shouldEqual true
  }

  "Transaction" should "have valid TxOut values." in {
    val pair: KeyPair = Secp256k1.generateKeyPair()

    val uTxOs: mutable.Map[Outpoint, TxOut] = mutable.Map.empty[Outpoint, TxOut]
    uTxOs += (Outpoint("def0", 1) -> TxOut("abc4", 20))

    val tx: Transaction = Transaction(
      Seq(TxIn(Outpoint("def0", 0), "abc1"), TxIn(Outpoint("def0", 1), "abc1")),
      Seq(TxOut("abc4", 40)),
      genesisTimestamp
    )

    validateTxOutValues(tx, uTxOs) shouldEqual false

    uTxOs += (Outpoint("def0", 0) -> TxOut("abc1", 20))
    validateTxOutValues(tx, uTxOs) shouldEqual true
  }

  "updateUTxOs" should "update the UTXOs from a latest Seq of transactions." in {
    val tx: Transaction = Transaction(
      Seq(TxIn(Outpoint("def0", 0), "abc1"),
        TxIn(Outpoint("def0", 1), "abc1")),
      Seq(TxOut("abc4", 40)),
      genesisTimestamp
    )

    val uTxOs1: mutable.Map[Outpoint, TxOut] = mutable.Map.empty[Outpoint, TxOut]
    uTxOs1 += (Outpoint("def0", 0) -> TxOut("abc4", 20))
    uTxOs1 += (Outpoint("def0", 1) -> TxOut("abc4", 20))

    val uTxOs2: mutable.Map[Outpoint, TxOut] = mutable.Map.empty[Outpoint, TxOut]
    updateUTxOs(Seq(tx), uTxOs1.toMap) should not equal uTxOs2

    uTxOs2 += (Outpoint(tx.id, 0) -> TxOut("abc4", 40))
    updateUTxOs(Seq(tx), uTxOs1.toMap) shouldEqual uTxOs2
  }

  "Transaction" should "have be validatable." in {
    val pair1 = Secp256k1.generateKeyPair()
    val address1 = pair1.getPublic.toHex
    val pair2 = Secp256k1.generateKeyPair()
    val address2 = pair2.getPublic.toHex
    val randHash = "".sha256
    val tx: Transaction = Transaction(
      Seq(TxIn(Outpoint(randHash, 0), ""),
        TxIn(Outpoint(randHash, 1), "")),
      Seq(TxOut(address2, 40)),
      genesisTimestamp
    )

    val uTxOs: mutable.Map[Outpoint, TxOut] = mutable.Map.empty[Outpoint, TxOut]
    val signedTxIns = tx.txIns.map(txIn => signTxIn(tx.id.hex2Bytes, txIn, pair1, uTxOs)).filter(_.isDefined).map(_.get)
    signedTxIns.length should not equal tx.txIns.length
    val signedTx = Transaction(
      signedTxIns,
      Seq(TxOut(address2, 40)),
      genesisTimestamp)

    signedTx.isValid(uTxOs) shouldEqual false

    uTxOs += (Outpoint(randHash, 0) -> TxOut(address1, 20))
    uTxOs += (Outpoint(randHash, 1) -> TxOut(address1, 20))

    val signedTxIns2 = tx.txIns.map(txIn => signTxIn(tx.id.hex2Bytes, txIn, pair1, uTxOs)).filter(_.isDefined).map(_.get)
    signedTxIns2.length shouldEqual tx.txIns.length
    val signedTx2 = Transaction(
      signedTxIns2,
      Seq(TxOut(address2, 40)),
      genesisTimestamp)

    signedTx2.isValid(uTxOs) shouldEqual true
  }

  "noDuplicateTxIn" should "detect whether Seq of Transactions contians duplicate TxIns." in {
    noDuplicateTxInOf(Seq.empty[Transaction]) shouldEqual true
    val tx1: Transaction = Transaction(
      Seq(TxIn(Outpoint("a", 0), ""),
        TxIn(Outpoint("b", 1), "")),
      Seq(TxOut("o", 40)),
      genesisTimestamp
    )
    noDuplicateTxInOf(Seq(tx1)) shouldEqual true
    val tx2: Transaction = Transaction(
      Seq(TxIn(Outpoint("c", 0), ""),
        TxIn(Outpoint("d", 1), "")),
      Seq(TxOut("o", 40)),
      genesisTimestamp
    )
    noDuplicateTxInOf(Seq(tx1, tx2)) shouldEqual true
    val tx3: Transaction = Transaction(
      Seq(TxIn(Outpoint("e", 0), ""),
        TxIn(Outpoint("a", 1), "")),
      Seq(TxOut("o", 40)),
      genesisTimestamp
    )
    noDuplicateTxInOf(Seq(tx1, tx2, tx3)) shouldEqual true
    val tx4: Transaction = Transaction(
      Seq(TxIn(Outpoint("a", 0), ""),
        TxIn(Outpoint("f", 1), "")),
      Seq(TxOut("o", 40)),
      genesisTimestamp
    )
    noDuplicateTxInOf(Seq(tx1, tx2, tx3, tx4)) shouldEqual false
  }


}
