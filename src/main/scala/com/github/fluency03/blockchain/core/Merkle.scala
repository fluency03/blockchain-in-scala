package com.github.fluency03.blockchain
package core

import com.github.fluency03.blockchain.crypto.SHA256

import scala.annotation.tailrec

// TODO (Chang): implement a full nodes merkle tree


object Merkle {
  def computeRoot(trans: Seq[Transaction]): String = computeRootOfHashes(trans.map(_.id))

  // TODO (Chang): double SHA256 hash instead of single.
  // TODO (Chang): hash and concatenate hash string or hash bytes?
  @tailrec
  def computeRootOfHashes(hashes: Seq[String]): String = hashes.length match {
    case 0 => ZERO64
    case 1 => hashes.head
    case n if n % 2 != 0 => computeRootOfHashes(hashes :+ hashes.last) // append last element again
    case _ => computeRootOfHashes(hashes.grouped(2).map { a => SHA256.hashAll(a(0), a(1)) } .toList)
  }

  @tailrec
  def hashViaMerklePath(init: String, path: Seq[String], index: Int): String =
    if (path.isEmpty) init
    else {
      val newHash = if (index % 2 == 0)
        SHA256.hashAll(init, path.head)
      else
        SHA256.hashAll(path.head, init)
      hashViaMerklePath(newHash, path.tail, index/2)
    }

  def verifySimplified(init: String, root: String, path: Seq[String], index: Int): Boolean =
    hashViaMerklePath(init, path, index) == root


}
