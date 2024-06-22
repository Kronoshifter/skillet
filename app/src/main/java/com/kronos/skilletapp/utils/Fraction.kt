package com.kronos.skilletapp.utils

import kotlin.math.abs
import kotlin.math.roundToInt

data class Fraction(val numerator: Int, val denominator: Int) {
  val decimal: Double
    get() = numerator.toDouble() / denominator



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

  fun toCompoundString(): String {
    val whole = numerator / denominator
    val subNumerator = numerator % denominator
    return if (whole == 0) "$subNumerator/$denominator" else "$whole $subNumerator/$denominator"
  }
}