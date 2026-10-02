package com.kronos.utils

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

private const val EIGHTH_SCALE = 8.0
private const val THIRD_SCALE = 3.0
private const val CLOSE_TO_ZERO_TOLERANCE = 0.001f

fun Double.roundToEighth() = (this * EIGHTH_SCALE).roundToInt() / EIGHTH_SCALE

fun Double.roundToThird() = (this * THIRD_SCALE).roundToInt() / THIRD_SCALE

fun Float.roundToNth(n: Number) = (this * n.toFloat()).roundToInt() / n.toFloat()

fun Float.isCloseEnoughToZero() = this.absoluteValue < CLOSE_TO_ZERO_TOLERANCE

val Float.nearestEighth
  get() = roundToNth(8)
val Float.nearestThird
  get() = roundToNth(3)

val Double.fraction
  get() = this.toFloat().fraction

val Float.fraction
  get() = Fraction((this * 1000).toInt(), 1000).reduce()

fun Double.roundToSignificantFigures(places: Int) = toBigDecimal().round(MathContext(places, RoundingMode.HALF_UP)).toDouble()

val ONE_EIGHTH = (BigDecimal(1) / BigDecimal(8)).setScale(3, RoundingMode.HALF_UP)

fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

fun lcm(a: Int, b: Int): Int = a * b / gcd(a, b)
