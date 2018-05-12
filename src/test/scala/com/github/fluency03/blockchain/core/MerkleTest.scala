package com.github.fluency03.blockchain
package core

import com.github.fluency03.blockchain.core.Merkle._
import com.github.fluency03.blockchain.core.Transaction.createCoinbaseTx
import com.github.fluency03.blockchain.crypto.SHA256
import org.scalatest.{FlatSpec, Matchers}

class MerkleTest extends FlatSpec with Matchers {

  "A empty Merkle Tree" should "have Zero 64 as root hash." in {
    computeRootOfHashes(List()) shouldEqual ZERO64
    computeRoot(List()) shouldEqual ZERO64
  }

  "A empty Merkle Tree with one element" should "have root hash as same as the only element hash." in {
    val h = "41ef4bb0b23661e66301aac36066912dac037827b4ae63a7b1165a5aa93ed4eb"
    computeRootOfHashes(List(h)) shouldEqual h

    val t: Transaction = createCoinbaseTx(0, genesisMiner, genesisTimestamp)
    computeRoot(List(t)) shouldEqual t.id
  }

  it should "be able to compute valid root hash." in {
    val h1 = "41ef4bb0b23661e66301aac36066912dac037827b4ae63a7b1165a5aa93ed4eb"
    val h2 = "000031bee3fa033f2d69ae7d0d9f565bf3a235452ccf8a5edffb78cfbcdd7137"
    val h12 = SHA256.hashAll(h1, h2)
    computeRootOfHashes(List(h1, h2)) shouldEqual h12

    val h3 = "000031beekdjnvj2310i0i0c4i3jomo1m2km10ijodsjco1edffb78cfbcdd7137"
    val h33 = SHA256.hashAll(h3, h3)
    computeRootOfHashes(List(h1, h2, h3)) shouldEqual SHA256.hashAll(h12, h33)

    val t1 = createCoinbaseTx(1, genesisMiner, genesisTimestamp)
    val t2 = createCoinbaseTx(2, genesisMiner, genesisTimestamp)
    val th12 = SHA256.hashAll(t1.id, t2.id)
    computeRoot(List(t1, t2)) shouldEqual th12

    val t3 = createCoinbaseTx(3, genesisMiner, genesisTimestamp)
    val th33 = SHA256.hashAll(t3.id, t3.id)
    computeRoot(List(t1, t2, t3)) shouldEqual SHA256.hashAll(th12, th33)
  }

  it should "be able to compute root hash via merkle path and verify it." in {
    hashViaMerklePath(
      "D97A21CF46FD5AFB0BF9EA4237BC4BF5C84E8B47D38D1EEE2BBEB5C0F8A1C625",
      Seq.empty[HexString],
      0) shouldEqual
      "D97A21CF46FD5AFB0BF9EA4237BC4BF5C84E8B47D38D1EEE2BBEB5C0F8A1C625".toLowerCase()

    hashViaMerklePath(
      "D97A21CF46FD5AFB0BF9EA4237BC4BF5C84E8B47D38D1EEE2BBEB5C0F8A1C625",
      Seq("AE1E670BDBF8AB984F412E6102C369AECA2CED933A1DE74712CCDA5EDAF4EE57"),
      0) shouldEqual
      "e2fbeaa16b00fb4ba139c62158971612aa8cddf6163082c74fa74ebb5004c10b"

    hashViaMerklePath(
      "D97A21CF46FD5AFB0BF9EA4237BC4BF5C84E8B47D38D1EEE2BBEB5C0F8A1C625",
      Seq(
        "AE1E670BDBF8AB984F412E6102C369AECA2CED933A1DE74712CCDA5EDAF4EE57",
        "EFC2B3DB87FF4F00C79DFA8F732A23C0E18587A73A839B7710234583CDD03DB9"),
      2) shouldEqual
      "dce52948923f07840400d52cc5deb037c8cef400a2e97699146a291112477ce0"

    hashViaMerklePath(
      "e2fbeaa16b00fb4ba139c62158971612aa8cddf6163082c74fa74ebb5004c10b",
      Seq("EFC2B3DB87FF4F00C79DFA8F732A23C0E18587A73A839B7710234583CDD03DB9"),
      1) shouldEqual
      "dce52948923f07840400d52cc5deb037c8cef400a2e97699146a291112477ce0"

    hashViaMerklePath(
      "D97A21CF46FD5AFB0BF9EA4237BC4BF5C84E8B47D38D1EEE2BBEB5C0F8A1C625",
      Seq(
        "AE1E670BDBF8AB984F412E6102C369AECA2CED933A1DE74712CCDA5EDAF4EE57",
        "EFC2B3DB87FF4F00C79DFA8F732A23C0E18587A73A839B7710234583CDD03DB9",
        "F1B6FE8FC2AB800E6D76EE975A002D3E67A60B51A62085A07289505B8D03F149"),
      6) shouldEqual
      hashViaMerklePath(
        "dce52948923f07840400d52cc5deb037c8cef400a2e97699146a291112477ce0",
        Seq("F1B6FE8FC2AB800E6D76EE975A002D3E67A60B51A62085A07289505B8D03F149"),
        1)

    hashViaMerklePath(
      "D97A21CF46FD5AFB0BF9EA4237BC4BF5C84E8B47D38D1EEE2BBEB5C0F8A1C625",
      Seq(
        "AE1E670BDBF8AB984F412E6102C369AECA2CED933A1DE74712CCDA5EDAF4EE57",
        "EFC2B3DB87FF4F00C79DFA8F732A23C0E18587A73A839B7710234583CDD03DB9",
        "F1B6FE8FC2AB800E6D76EE975A002D3E67A60B51A62085A07289505B8D03F149",
        "E827331B1FE7A2689FBC23D14CD21317C699596CBCA222182A489322ECE1FA74"),
      6) shouldEqual
      hashViaMerklePath(
        hashViaMerklePath(
          "dce52948923f07840400d52cc5deb037c8cef400a2e97699146a291112477ce0",
          Seq("F1B6FE8FC2AB800E6D76EE975A002D3E67A60B51A62085A07289505B8D03F149"),
          1),
        Seq("E827331B1FE7A2689FBC23D14CD21317C699596CBCA222182A489322ECE1FA74"),
        0)

    verifySimplified(
      "D97A21CF46FD5AFB0BF9EA4237BC4BF5C84E8B47D38D1EEE2BBEB5C0F8A1C625",
      "79f56ece2f6f9082bf36c6f131bbe85ac4a3f0c5a07527e29f19e78c5bc281f8",
      Seq(
        "AE1E670BDBF8AB984F412E6102C369AECA2CED933A1DE74712CCDA5EDAF4EE57",
        "EFC2B3DB87FF4F00C79DFA8F732A23C0E18587A73A839B7710234583CDD03DB9",
        "F1B6FE8FC2AB800E6D76EE975A002D3E67A60B51A62085A07289505B8D03F149",
        "E827331B1FE7A2689FBC23D14CD21317C699596CBCA222182A489322ECE1FA74"),
      6) shouldEqual true

  }

}
