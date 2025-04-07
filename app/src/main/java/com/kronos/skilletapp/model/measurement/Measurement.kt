package com.kronos.skilletapp.model.measurement

import com.github.michaelbull.result.*
import com.kronos.skilletapp.utils.roundToEighth
import kotlin.math.roundToInt
import com.kronos.skilletapp.utils.haveSameTypes
import com.kronos.skilletapp.utils.toFraction
import kotlinx.serialization.Serializable
import kotlin.collections.filter

@Serializable
data class Measurement(
  val quantity: Float,
  val unit: MeasurementUnit,
) {
  operator fun times(factor: Float) = scale(factor)
  operator fun times(factor: Double) = times(factor.toFloat())
  operator fun times(factor: Int) = times(factor.toFloat())

  operator fun div(divisor: Float) = scale(1 / divisor)
  operator fun div(divisor: Double) = div(divisor.toFloat())
  operator fun div(divisor: Int) = div(divisor.toFloat())

  operator fun unaryMinus() = copy(quantity = -quantity)

  operator fun plus(other: Measurement) = when (unit) {
    other.unit -> copy(quantity = quantity + other.quantity)
    else -> copy(quantity = quantity + other.convert(unit).quantity)
  }

  operator fun minus(other: Measurement) = when (unit) {
    other.unit -> copy(quantity = quantity - other.quantity)
    else -> copy(quantity = quantity - other.convert(unit).quantity)
  }

  operator fun inc() = copy(quantity = quantity + 1)
  operator fun dec() = copy(quantity = quantity - 1)

  //  operator fun compareTo(other: Measurement) = (amount * unit.factor).compareTo(other.amount * other.unit.factor)
  operator fun compareTo(other: Measurement): Int {
    val result = this - other
    return when {
      result.quantity in -0.001..0.001 -> 0
      result.quantity < 0 -> -1
      else -> 1
    }
  }

  override fun equals(other: Any?): Boolean {
    return other?.let {
      (it as? Measurement)?.let { that ->
        (this - that).quantity in -0.001..0.001
      } ?: false
    } ?: false
  }

  override fun hashCode(): Int {
    var result = quantity.hashCode()
    result = 31 * result + unit.hashCode()
    return result
  }

  override fun toString(): String {
    return when (unit) {
      is MeasurementSystem.Metric -> "${quantity.toString().take(4).removeSuffix(".")} ${unit.name}"
      else -> "${quantity.toFraction().roundToNearestFraction().reduce()} ${unit.name}"
    }
  }

  fun scale(factor: Float) = copy(quantity = quantity * factor)

  private fun convert(to: MeasurementUnit.Mass) = convert(to) {
    check(unit is MeasurementUnit.Mass)
    it * unit.factor / to.factor
  }

  private fun convert(to: MeasurementUnit.Volume) = convert(to) {
    check(unit is MeasurementUnit.Volume)
    it * unit.factor / to.factor
  }

  fun convert(to: MeasurementUnit) = when (unit) {
    is MeasurementUnit.Mass -> convert(to as MeasurementUnit.Mass)
    is MeasurementUnit.Volume -> convert(to as MeasurementUnit.Volume)
    is MeasurementUnit.Custom -> copy(unit = to)
    MeasurementUnit.None -> this
  }

  fun convert(to: MeasurementUnit, converter: (Float) -> Float) = Measurement(converter(quantity), unit = to)


  fun scaleAndNormalize(factor: Float) = scale(factor).normalize()

  fun normalize(filter: ((MeasurementUnit) -> Boolean)? = null): Measurement {
    var normalized = copy()
    while (normalized.quantity !in normalized.unit.normalizationLow..<normalized.unit.normalizationHigh) {
      if (normalized.quantity <= normalized.unit.normalizationLow) {
        normalized = normalized.convert(normalized.unit.previous(filter).expect { "No previous unit, normalization range for ${unit.name} configured incorrectly" })
      } else if (normalized.quantity >= normalized.unit.normalizationHigh) {
        normalized = normalized.convert(normalized.unit.next(filter).expect { "No previous unit, normalization range for ${unit.name} configured incorrectly" })
      }
    }
    return normalized
  }

  fun roundToEighth() = copy(quantity = quantity.roundToEighth())
  fun round() = copy(quantity = quantity.roundToInt().toFloat())

  fun scaleAndRound(factor: Float) = scale(factor).roundToEighth()

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
  val filtered = MeasurementUnit.values.filter { it hasSameDimension this }.filter { it hasSameSystemAs this }.filter { filter?.invoke(it) != false }
  return filtered.getOrNull(filtered.indexOf(this) + 1).toResultOr { }
}

fun MeasurementUnit.previous(filter: ((MeasurementUnit) -> Boolean)? = null): Result<MeasurementUnit, Unit> {
  val filtered = MeasurementUnit.values.filter { it hasSameDimension this }.filter { it hasSameSystemAs this }.filter { filter?.invoke(it) != false }
  return filtered.getOrNull(filtered.indexOf(this) - 1).toResultOr { }
}

infix fun MeasurementUnit.hasSameDimension(other: MeasurementUnit) = (this to other).haveSameTypes(*MeasurementDimension::class.nestedClasses.toTypedArray())
infix fun MeasurementUnit.hasSameSystemAs(other: MeasurementUnit) = (this to other).haveSameTypes(*MeasurementSystem::class.nestedClasses.toTypedArray())
infix fun Number.of(unit: MeasurementUnit): Measurement = Measurement(this.toFloat(), unit)
