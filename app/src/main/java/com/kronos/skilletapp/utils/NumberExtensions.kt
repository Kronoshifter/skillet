package com.kronos.skilletapp.utils

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.roundToInt

//fun Double.roundToEighth(): BigDecimal {
//  val result = toBigDecimal().setScale(3, RoundingMode.CEILING) * BigDecimal(8)
//  val rounded = result.setScale(0, RoundingMode.HALF_UP)
//  return rounded.setScale(3, RoundingMode.CEILING) / BigDecimal(8).setScale(3, RoundingMode.CEILING)
//}

fun Double.roundToEighth() = (this * 8.0).roundToInt() / 8.0

val ONE_EIGHTH = (BigDecimal(1) / BigDecimal(8)).setScale(3, RoundingMode.HALF_UP)

fun gcd(a: Int, b: Int): Int {
  return if (b == 0) a else gcd(b, a % b)
}