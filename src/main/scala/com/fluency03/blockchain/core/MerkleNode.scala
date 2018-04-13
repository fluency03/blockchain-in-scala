package com.fluency03.blockchain.core

import com.fluency03.blockchain.Util.hashOf

object MerkleNode {
  def computeRoot(trans: List[Transaction]): String =
    computeRootOfHashes(trans.map(_.hash))

  def computeRootOfHashes(hashes: List[String]): String = hashes.length match {
    case 0 => ZERO64
    case 1 => hashes.head
    case n if n % 2 != 0 => computeRootOfHashes(hashes :+ hashes.last) // append last element again
    case _ => computeRootOfHashes(hashes.grouped(2).map { case a :: b :: Nil => hashOf(a,b) } .toList)
  }

}
