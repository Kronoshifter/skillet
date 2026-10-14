package com.kronos.utils

import kotlin.math.abs
import kotlin.math.roundToInt

data class Fraction(val numerator: Int, val denominator: Int) {
  constructor(
    whole: Int,
    numerator: Int,
    denominator: Int,
  ) : this(whole * denominator + numerator, denominator)

  val decimal: Float
    get() = numerator.toFloat() / denominator

  val whole: Int
    get() = numerator / denominator

  val subNumerator: Int
    get() = numerator % denominator

  operator fun unaryMinus(): Fraction = Fraction(-numerator, denominator)

  operator fun plus(other: Fraction): Fraction =
    if (denominator == other.denominator) {
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

  override fun toString(): String =
    when {
      numerator == 0 -> "0"
      whole == 0 -> simpleFractionString(numerator, denominator)
      subNumerator == 0 -> "$whole"
      else -> "$whole ${simpleFractionString(subNumerator, denominator)}"
    }

  fun toDisplayString(): String =
    when {
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
    val (n, _) =
      diffs.minWith { a, b ->
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
}

private val fractionGlyphs =
  mapOf(
    2 to mapOf(1 to "½"),
    3 to mapOf(1 to "⅓", 2 to "⅔"),
    4 to mapOf(1 to "¼", 3 to "¾"),
    5 to mapOf(1 to "⅕", 2 to "⅖", 3 to "⅗", 4 to "⅘"),
    6 to mapOf(1 to "⅙", 5 to "⅚"),
    8 to mapOf(1 to "⅛", 3 to "⅜", 5 to "⅝", 7 to "⅞"),
  )

private fun unicodeFractionString(numerator: Int, denominator: Int) =
  fractionGlyphs[denominator]?.get(numerator) ?: simpleFractionString(numerator, denominator)

private fun simpleFractionString(numerator: Int, denominator: Int) = "$numerator$FRAC$denominator"

private const val FRAC = "⁄"
