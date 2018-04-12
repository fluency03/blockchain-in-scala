package com.fluency03.blockchain.core

import com.fluency03.blockchain.Util.hashOf

class MerkleNode {





}



object MerkleNode {

  def computeRoot(trans: List[Transaction]): String =
    computeRootOfHashes(trans.map(_.hash))

  def computeRootOfHashes(hashes: List[String]): String = hashes.length match {
    case 1 => hashes.head
    case n if n % 2 != 0 => computeRootOfHashes(hashes :+ hashes.last) // append last element again
    case _ => computeRootOfHashes(hashes.zip(hashes.tail).map {case (h1, h2) => hashOf(h1, h2)})
  }


}
