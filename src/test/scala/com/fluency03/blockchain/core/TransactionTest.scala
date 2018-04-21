package com.fluency03.blockchain.core

import java.time.Instant

import com.fluency03.blockchain.Util.hashOf
import com.fluency03.blockchain.core.Transaction.createCoinbaseTx
import org.json4s.JValue
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods.parse
import org.scalatest.{FlatSpec, Matchers}

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






}
