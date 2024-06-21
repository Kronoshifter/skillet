package com.kronos.skilletapp.utils

data class Fraction(val numerator: Int, val denominator: Int) {
  val decimal: Double
    get() = numerator.toDouble() / denominator

  fun reduce(): Fraction {
    val gcd = gcd(numerator, denominator)
    return Fraction(numerator / gcd, denominator / gcd)
  }

  fun toCompoundString(): String {
    val whole = numerator / denominator
    val numerator = numerator % denominator
    return if (whole == 0) "$numerator/$denominator" else "$whole $numerator/$denominator"
  }

  companion object {
    fun from(decimal: Double): Fraction {
      val numerator = (decimal.roundToEighth() * 1000).toInt()
      val denominator = 1000
      return Fraction(numerator, denominator).reduce()
    }
  }
}