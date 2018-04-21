package com.fluency03.blockchain.core

import com.fluency03.blockchain.Util.hashOf

object Merkle {
  def computeRoot(trans: Seq[Transaction]): String =
    computeRootOfHashes(trans.map(_.id))

  def computeRootOfHashes(hashes: Seq[String]): String = hashes.length match {
    case 0 => ZERO64
    case 1 => hashes.head
    case n if n % 2 != 0 => computeRootOfHashes(hashes :+ hashes.last) // append last element again
    case _ => computeRootOfHashes(hashes.grouped(2).map { a => hashOf(a(0), a(1)) } .toList)
  }

}
