package com.kronos.skilletapp.utils

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

fun Double.roundToEighth(): BigDecimal {
  val result = toBigDecimal().setScale(3, RoundingMode.CEILING) * BigDecimal(8)
  val rounded = result.setScale(0, RoundingMode.HALF_UP)
  return rounded.setScale(3, RoundingMode.CEILING) / BigDecimal(8).setScale(3, RoundingMode.CEILING)
}