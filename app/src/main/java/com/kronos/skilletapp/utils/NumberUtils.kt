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

fun Float.roundToNth(n: Int) = (this * n.toFloat()).roundToInt() / n.toFloat()

val Float.nearestEighth get() = roundToNth(8)
val Float.nearestThird get() = roundToNth(3)

val Double.fraction
  get() = this.toFloat().fraction

val Float.fraction
  get() = Fraction((this * 1000).toInt(), 1000).reduce()

fun Double.roundToSignificantFigures(places: Int) = toBigDecimal().round(MathContext(places, RoundingMode.HALF_UP)).toDouble()

val ONE_EIGHTH = (BigDecimal(1) / BigDecimal(8)).setScale(3, RoundingMode.HALF_UP)

fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

fun lcm(a: Int, b: Int): Int = a * b / gcd(a, b)
