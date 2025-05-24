package com.kronos.skilletapp.model.measurement

import com.github.michaelbull.result.expect
import com.kronos.skilletapp.utils.fraction
import com.kronos.skilletapp.utils.nearestEighth
import kotlinx.serialization.Serializable

@Serializable
data class Measurement(
  val quantity: Float,
  val unit: MeasurementUnit,
) {
  operator fun times(factor: Number) = scale(factor.toFloat())
  operator fun div(divisor: Number) = scale(1f / divisor.toFloat())

  operator fun unaryMinus() = copy(quantity = -quantity)

  operator fun plus(other: Number) = copy(quantity = quantity + other.toFloat())
  operator fun plus(other: Measurement): Measurement {
    require(unit hasSameDimensionAs other.unit) { "Units must measure the same dimension to properly add" }
    return when (unit) {
      other.unit -> copy(quantity = quantity + other.quantity)
      else -> copy(quantity = quantity + other.convertTo(unit).quantity)
    }
  }

  operator fun minus(other: Number) = copy(quantity = quantity - other.toFloat())
  operator fun minus(other: Measurement): Measurement {
    require(unit hasSameDimensionAs other.unit) { "Units must measure the same dimension to properly subtract" }
    return when (unit) {
      other.unit -> copy(quantity = quantity - other.quantity)
      else -> copy(quantity = quantity - other.convertTo(unit).quantity)
    }
  }

  operator fun inc() = copy(quantity = quantity + 1f)
  operator fun dec() = copy(quantity = quantity - 1f)

  operator fun compareTo(other: Measurement): Int {
    val result = this - other
    return when {
      result.quantity in -0.001..0.001 -> 0
      result.quantity < 0 -> -1
      else -> 1
    }
  }

  fun scale(factor: Float) = copy(quantity = quantity * factor)

  override fun toString(): String {
    return when (unit.system) {
      is MeasurementSystem.Metric -> "${quantity.toString().take(4).removeSuffix(".")} ${unit.name}"
      else -> "${quantity.fraction.roundToNearestFraction().reduce()} ${unit.name}"
    }
  }

  val displayQuantity
    get() = when (unit.system) {
      is MeasurementSystem.Metric -> quantity.toString().take(4).removeSuffix(".")
      else -> quantity.fraction.roundToNearestFraction().reduce().toDisplayString()
    }

  companion object {
    val None = Measurement(0f, MeasurementUnit.None)
  }
}

fun Measurement.roundToEighth() = copy(quantity = quantity.nearestEighth)
fun Measurement.normalized(filter: ((MeasurementUnit) -> Boolean)? = null): Measurement {
  var normalized = copy()

  while (normalized.quantity !in normalized.unit.normalizationLow ..< normalized.unit.normalizationHigh) {
    with(normalized) {
      if (quantity <= unit.normalizationLow) {
        normalized = normalized convertTo unit.previous(filter).expect { "No previous unit, normalization range for ${unit.name} configured incorrectly" }
      } else if (quantity >= unit.normalizationHigh) {
        normalized = normalized convertTo unit.next(filter).expect { "No previous unit, normalization range for ${unit.name} configured incorrectly" }
      }
    }
  }

  return normalized
}

infix fun Number.of(unit: MeasurementUnit): Measurement = Measurement(toFloat(), unit)
fun Measurement.isNone() = this == Measurement.None
fun Measurement.isNotNone() = !isNone()

infix fun Measurement.isEquivalentTo(other: Measurement): Boolean = when {
  isNone() -> other.isNone()
  other.isNone() -> isNone()
  unit is MeasurementUnit.Custom && other.unit is MeasurementUnit.Custom && unit.name != other.unit.name -> false
  unit == other.unit -> (quantity - other.quantity) in -0.001f..0.001f
  unit hasSameDimensionAs other.unit -> (this - other).quantity in -0.001f..0.001f
  else -> false
}