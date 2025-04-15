package com.kronos.skilletapp.model.measurement

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.expect
import com.github.michaelbull.result.toResultOr
import com.kronos.skilletapp.utils.haveSameTypes
import com.kronos.skilletapp.utils.roundToEighth
import com.kronos.skilletapp.utils.toFraction
import kotlinx.serialization.Serializable
import kotlin.math.absoluteValue

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

  override fun toString(): String {
    return when (unit) {
      is MeasurementSystem.Metric -> "${quantity.toString().take(4).removeSuffix(".")} ${unit.name}"
      else -> "${quantity.toFraction().roundToNearestFraction().reduce()} ${unit.name}"
    }
  }

  fun scale(factor: Float) = copy(quantity = quantity * factor)

  fun normalized(filter: ((MeasurementUnit) -> Boolean)? = null): Measurement {
    var normalized = copy()

    while (normalized.quantity !in normalized.unit.normalizationLow..<normalized.unit.normalizationHigh) {
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

  fun roundToEighth() = copy(quantity = quantity.roundToEighth())

  val displayQuantity
    get() = when (unit) {
      is MeasurementSystem.Metric -> quantity.toString().take(4).removeSuffix(".")
      else -> quantity.toFraction().roundToNearestFraction().reduce().toDisplayString()
    }

  companion object {
    val None = Measurement(0f, MeasurementUnit.None)
  }
}

fun MeasurementUnit.next(filter: ((MeasurementUnit) -> Boolean)? = null): Result<MeasurementUnit, Unit> {
  val filtered = MeasurementUnit.values.filter { it hasSameDimensionAs this }.filter { it hasSameSystemAs this }.filter { filter?.invoke(it) != false }
  return filtered.getOrNull(filtered.indexOf(this) + 1).toResultOr { }
}

fun MeasurementUnit.previous(filter: ((MeasurementUnit) -> Boolean)? = null): Result<MeasurementUnit, Unit> {
  val filtered = MeasurementUnit.values.filter { it hasSameDimensionAs this }.filter { it hasSameSystemAs this }.filter { filter?.invoke(it) != false }
  return filtered.getOrNull(filtered.indexOf(this) - 1).toResultOr { }
}

infix fun MeasurementUnit.hasSameDimensionAs(other: MeasurementUnit) = (this to other).haveSameTypes(*MeasurementDimension::class.nestedClasses.toTypedArray())
infix fun MeasurementUnit.hasSameSystemAs(other: MeasurementUnit) = (this to other).haveSameTypes(*MeasurementSystem::class.nestedClasses.toTypedArray())
infix fun Number.of(unit: MeasurementUnit): Measurement = Measurement(this.toFloat(), unit)
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