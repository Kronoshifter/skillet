package com.kronos.skilletapp.utils

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.roundToInt

//fun Double.roundToEighth(): BigDecimal {
//  val result = toBigDecimal().setScale(3, RoundingMode.CEILING) * BigDecimal(8)
//  val rounded = result.setScale(0, RoundingMode.HALF_UP)
//  return rounded.setScale(3, RoundingMode.CEILING) / BigDecimal(8).setScale(3, RoundingMode.CEILING)
//}

fun Double.roundToEighth() = (this * 8.0).roundToInt() / 8.0
fun Double.roundToThird() = (this * 3.0).roundToInt() / 3.0
fun Double.roundToNearestFraction(): Double {
  val eighthDiff = abs(this - roundToEighth())
  val thirdDiff = abs(this - roundToThird())
  return if (eighthDiff < thirdDiff) {
    roundToEighth()
  } else {
    roundToThird()
  }
}

fun Double.toFraction(): Fraction {
  val numerator = (this * 1000).toInt()
  val denominator = 1000
  return Fraction(numerator, denominator).reduce()
}

val ONE_EIGHTH = (BigDecimal(1) / BigDecimal(8)).setScale(3, RoundingMode.HALF_UP)

fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

fun lcm(a: Int, b: Int): Int = a * b / gcd(a, b)

fun fractionString(numerator: Int, denominator: Int) = when(denominator) {
  2 -> if (numerator == 1) "\u00BD" else "$numerator/$denominator"
  3 -> when(numerator) {
    1 -> "\u2153"
    2 -> "\u2154"
    else -> "$numerator/$denominator"
  }
  4 -> when(numerator) {
    1 -> "\u00BC"
    3 -> "\u00BE"
    else -> "$numerator/$denominator"
  }
  8 -> when(numerator) {
    1 -> "\u215B"
    3 -> "\u215C"
    5 -> "\u215D"
    7 -> "\u215E"
    else -> "$numerator/$denominator"
  }
  else -> "$numerator/$denominator"
}