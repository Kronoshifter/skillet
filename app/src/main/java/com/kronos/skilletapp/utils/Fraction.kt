package com.kronos.skilletapp.utils

import kotlin.math.abs
import kotlin.math.roundToInt

data class Fraction(val numerator: Int, val denominator: Int) {
  val decimal: Double
    get() = numerator.toDouble() / denominator

  val whole: Int
    get() = numerator / denominator

  val subNumerator: Int
    get() = numerator % denominator

  operator fun unaryMinus(): Fraction = Fraction(-numerator, denominator)

  operator fun plus(other: Fraction): Fraction = if (denominator == other.denominator) {
    Fraction(numerator + other.numerator, denominator)
  } else {
    val lcm = lcm(denominator, other.denominator)
    Fraction(numerator * lcm + other.numerator * lcm, denominator * lcm)
  }

  operator fun minus(other: Fraction): Fraction = this + (-other)

  operator fun times(other: Fraction): Fraction = Fraction(numerator * other.numerator, denominator * other.denominator)

  operator fun times(other: Int): Fraction = Fraction(numerator * other, denominator)

  operator fun div(other: Fraction): Fraction = Fraction(numerator * other.denominator, denominator * other.numerator)
  operator fun div(other: Int): Fraction = Fraction(numerator, denominator * other)

  operator fun compareTo(other: Fraction): Int = decimal.compareTo(other.decimal)
  operator fun compareTo(other: Int): Int = decimal.compareTo(other.toDouble())

  override fun toString(): String = when {
    numerator == 0 -> "0"
    whole == 0 -> simpleFractionString(numerator, denominator)
    subNumerator == 0 -> "$whole"
    else -> "$whole ${simpleFractionString(subNumerator, denominator)}"
  }

  fun toDisplayString(): String = when {
    numerator == 0 -> "0"
    whole == 0 -> unicodeFractionString(numerator, denominator)
    subNumerator == 0 -> "$whole"
    else -> "$whole ${unicodeFractionString(subNumerator, denominator)}"
  }

  fun reduce(): Fraction {
    val gcd = gcd(numerator, denominator)
    return Fraction(numerator / gcd, denominator / gcd)
  }

  fun roundToEighth(): Fraction {
    return Fraction(((numerator * 8).toDouble() / denominator).roundToInt(), 8)
  }

  fun roundToThird(): Fraction {
    return Fraction(((numerator * 3).toDouble() / denominator).roundToInt(), 3)
  }

  fun roundToNearestFraction(): Fraction {
    val eighthDiff = abs(decimal - decimal.roundToEighth())
    val thirdDiff = abs(decimal - decimal.roundToThird())
    return if (eighthDiff < thirdDiff) {
      roundToEighth()
    } else {
      roundToThird()
    }
  }

  private fun unicodeFractionString(numerator: Int, denominator: Int) = when(denominator) {
    2 -> if (numerator == 1) "\u00BD" else simpleFractionString(numerator, denominator)
    3 -> when(numerator) {
      1 -> "\u2153"
      2 -> "\u2154"
      else -> simpleFractionString(numerator, denominator)
    }
    4 -> when(numerator) {
      1 -> "\u00BC"
      3 -> "\u00BE"
      else -> simpleFractionString(numerator, denominator)
    }
    else -> simpleFractionString(numerator, denominator)
  }

  private fun simpleFractionString(numerator: Int, denominator: Int) = "$numerator/$denominator"
}