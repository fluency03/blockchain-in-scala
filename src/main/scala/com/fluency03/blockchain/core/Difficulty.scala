package com.fluency03.blockchain.core

object Difficulty {

  lazy val difficultyOneTarget: BigInt = targetOfBits("1d00ffff".hex)

  /**
   * https://github.com/bitcoin/bitcoin/blob/master/src/arith_uint256.cpp#L206
   */
  def decodeCompact(nCompact: Long): (BigInt, Boolean, Boolean) = {
    val nSize = (nCompact >> 24).toInt
    val (nWord, result) = if (nSize <= 3) {
      val nWord1 = (nCompact & 0x007fffff) >> 8 * (3 - nSize)
      (nWord1, BigInt(nWord1))
    } else {
      val nWord1 = nCompact & 0x007fffff
      (nWord1, BigInt(nWord1) << (8 * (nSize - 3)))
    }
    val fNegative = nWord != 0 && (nCompact & 0x00800000) != 0
    val fOverflow = nWord != 0 && ((nSize > 34) || (nWord > 0xff && nSize > 33) || (nWord > 0xffff && nSize > 32))
    (result, fNegative, fOverflow)
  }

//  def encodeCompact(target: BigInt, fNegative: Boolean): Long = {
//    val bitLength = target.bitLength
//    var nSize = ((if (bitLength == 0) 0 else bitLength + 1) + 7) / 8
//    var nCompact: Long = if (nSize <= 3) {
//      getLowBits(target, 64) << 8 * (3 - nSize)
//    } else {
//      val bn = target >> 8 * (nSize - 3)
//      getLowBits(bn, 64)
//    }
//    // The 0x00800000 bit denotes the sign.
//    // Thus, if it is already set, divide the mantissa by 256 and increase the exponent.
//    if ((nCompact & 0x00800000) != 0) (nCompact >>= 8, nSize += 1)
//
//    assert((nCompact & ~0x007fffff) == 0)
//    assert(nSize < 256)
//    nCompact |= nSize << 24
//    nCompact |= (if (fNegative && ((nCompact & 0x007fffff) != 0)) 0x00800000 else 0)
//    nCompact
//  }

  def getLowBits(x: BigInt, N: Int): Long = (x & ((1 << N) - 1)).toLong

  def targetOfBits(bitsInt: Long): BigInt = {
    val (result, fNegative, _) = decodeCompact(bitsInt)
    if (fNegative) -result else result
  }

//  def bitsOfTarget(target: BigInt, fNegative: Boolean): Long = encodeCompact(target, fNegative)

  def padHexTarget(hex: String): String = hex.length match {
    case len => "0" * (64 - len) + hex
  }

  def hashLessThanTarget(hash: String, target: String): Boolean = ???

  def difficultyOf(target: BigInt, negative: Boolean, overflow: Boolean): Double = {
    if (target == 0 || negative || overflow) 0.0
    else (BigDecimal(difficultyOneTarget) / BigDecimal(target)).toDouble
  }


}
