package com.kronos.skilletapp.utils

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.*

//fun Double.roundToEighth(): BigDecimal {
//  val result = toBigDecimal().setScale(3, RoundingMode.CEILING) * BigDecimal(8)
//  val rounded = result.setScale(0, RoundingMode.HALF_UP)
//  return rounded.setScale(3, RoundingMode.CEILING) / BigDecimal(8).setScale(3, RoundingMode.CEILING)
//}

fun Double.roundToEighth() = (this * 8.0).roundToInt() / 8.0
fun Double.roundToThird() = (this * 3.0).roundToInt() / 3.0

fun Float.roundToEighth() = (this * 8f).roundToInt() / 8f
fun Float.roundToThird() = (this * 3f).roundToInt() / 3f

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

fun Float.toFraction(): Fraction {
  val numerator = (this * 1000).toInt()
  val denominator = 1000
  return Fraction(numerator, denominator).reduce()
}

fun Double.roundToSignificantFigures(places: Int) = toBigDecimal().round(MathContext(places, RoundingMode.HALF_UP)).toDouble()

val ONE_EIGHTH = (BigDecimal(1) / BigDecimal(8)).setScale(3, RoundingMode.HALF_UP)

fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

fun lcm(a: Int, b: Int): Int = a * b / gcd(a, b)
