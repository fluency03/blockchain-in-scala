package com.fluency03.blockchain
package core

import scala.annotation.tailrec

object Merkle {
  def computeRoot(trans: Seq[Transaction]): String = computeRootOfHashes(trans.map(_.id))

  @tailrec
  def computeRootOfHashes(hashes: Seq[String]): String = hashes.length match {
    case 0 => ZERO64
    case 1 => hashes.head
    case n if n % 2 != 0 => computeRootOfHashes(hashes :+ hashes.last) // append last element again
    case _ => computeRootOfHashes(hashes.grouped(2).map { a => sha256Of(a(0), a(1)) } .toList)
  }

}
