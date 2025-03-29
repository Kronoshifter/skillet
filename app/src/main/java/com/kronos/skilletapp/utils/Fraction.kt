package com.kronos.skilletapp.utils

import kotlin.math.abs
import kotlin.math.roundToInt

data class Fraction(val numerator: Int, val denominator: Int) {
  constructor(whole: Int, numerator: Int, denominator: Int) : this(whole * denominator + numerator, denominator)

  val decimal: Float
    get() = numerator.toFloat() / denominator

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
  operator fun compareTo(other: Int): Int = decimal.compareTo(other.toFloat())

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

  fun roundToNth(n: Int): Fraction = Fraction(((numerator * n).toFloat() / denominator).roundToInt(), n)

  val nearestEighth: Fraction
    get() = roundToNth(8)

  val nearestThird: Fraction
    get() = roundToNth(3)

  fun roundToNearestFraction(): Fraction = nearest

  fun roundToNearestOf(vararg nums: Int): Fraction {
    val diffs = nums.associateWith { abs(decimal - decimal.roundToNth(it)) }
    val (n, _) = diffs.minWith { a, b ->
      val diff = a.value - b.value
      when (diff) {
        in 0.001f..Float.POSITIVE_INFINITY -> 1
        in Float.NEGATIVE_INFINITY..<0f -> -1
        else -> 0
      }
    }
    return roundToNth(n)
  }

  val nearest: Fraction
    get() = roundToNearestOf(3, 8)

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
    5 -> when(numerator) {
      1 -> "\u2155"
      2 -> "\u2156"
      3 -> "\u2157"
      4 -> "\u2158"
      else -> simpleFractionString(numerator, denominator)
    }
    6 -> when(numerator) {
      1 -> "\u2159"
      5 -> "\u215A"
      else -> simpleFractionString(numerator, denominator)
    }
    8 -> when(numerator) {
      1 -> "\u215B"
      3 -> "\u215C"
      5 -> "\u215D"
      7 -> "\u215E"
      else -> simpleFractionString(numerator, denominator)
    }
    else -> simpleFractionString(numerator, denominator)
  }

  private fun simpleFractionString(numerator: Int, denominator: Int) = "$numerator$FRAC$denominator"

  companion object {
    private const val FRAC = "\u2044"
  }
}