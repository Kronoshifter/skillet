package com.kronos.skilletapp.model

import com.github.michaelbull.result.*
import com.kronos.skilletapp.utils.roundToEighth

data class Measurement(
  val amount: Double,
  val unit: MeasurementUnit,
) {
  operator fun times(factor: Double) = scale(factor)
  operator fun times(factor: Int) = scale(factor.toDouble())

  operator fun div(factor: Double) = scale(1 / factor)
  operator fun div(factor: Int) = scale(1 / factor.toDouble())

  operator fun unaryMinus() = copy(amount = -amount)

  operator fun plus(other: Measurement) = when (unit) {
    other.unit -> copy(amount = amount + other.amount)
    else -> copy(amount = amount + other.convert(unit).amount)
  }
  operator fun minus(other: Measurement) = when (unit) {
    other.unit -> copy(amount = amount - other.amount)
    else -> copy(amount = amount - other.convert(unit).amount)
  }

  operator fun inc() = copy(amount = amount + 1)
  operator fun dec() = copy(amount = amount - 1)

  operator fun compareTo(other: Measurement) = (amount * unit.factor).compareTo(other.amount * other.unit.factor)

  fun scale(factor: Double) = copy(amount = amount * factor)

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
  }

  fun convert(to: MeasurementUnit, converter: (Double) -> Double) = Measurement(converter(amount), unit = to)
  

  fun scaleAndNormalize(factor: Double) = scale(factor).normalize()

  fun normalize(): Measurement {
    var normalized = copy()
    while (normalized.amount !in normalized.unit.normalRange) {
      val low = normalized.unit.normalRange.start
      val high = normalized.unit.normalRange.endExclusive

      if (normalized.amount <= low) {
        normalized = normalized.convert(normalized.unit.previous().unwrap())
      } else if (normalized.amount >= high) {
        normalized = normalized.convert(normalized.unit.next().unwrap())
      }
    }
    return normalized
  }

  fun roundToEighth() = copy(amount = amount.roundToEighth())

  fun scaleAndRound(factor: Double) = scale(factor).roundToEighth()
}

fun MeasurementUnit.next(): Result<MeasurementUnit, Unit> {
  val filtered = MeasurementUnit.values.filter { it.type == this.type }.filter { it.system == this.system }
  return filtered.getOrNull(filtered.indexOf(this) + 1).toResultOr {  }
}

fun MeasurementUnit.previous(): Result<MeasurementUnit, Unit> {
  val filtered = MeasurementUnit.values.filter { it.type == this.type }.filter { it.system == this.system }
  return filtered.getOrNull(filtered.indexOf(this) - 1).toResultOr {  }
}

enum class MeasurementType {
  Mass,
  Volume,
  Other
}

enum class MeasurementSystem {
  Metric,
  Imperial,
  Other
}

sealed class MeasurementUnit(
  open val name: String,
  open val factor: Double,
  open val abbreviation: String,
  open val system: MeasurementSystem,
  open val normalRange: OpenEndRange<Double>,
  open val type: MeasurementType,
) {

  sealed class Mass(
    override val name: String,
    override val factor: Double,
    override val abbreviation: String,
    override val normalRange: OpenEndRange<Double>,
    override val system: MeasurementSystem,
  ) : MeasurementUnit(
    name = name,
    factor = factor,
    abbreviation = abbreviation,
    system = system,
    normalRange = normalRange,
    type = MeasurementType.Mass)

  sealed class Volume(
    override val name: String,
    override val factor: Double,
    override val abbreviation: String,
    override val normalRange: OpenEndRange<Double>,
    override val system: MeasurementSystem,
  ) : MeasurementUnit(
    name = name,
    factor = factor,
    abbreviation = abbreviation,
    system = system,
    normalRange = normalRange,
    type = MeasurementType.Volume
  )

  data class Custom(
    override val name: String,
    override val factor: Double,
    override val abbreviation: String,
    override val normalRange: OpenEndRange<Double>
  ) : MeasurementUnit(
    name = name,
    factor = factor,
    abbreviation = abbreviation,
    system = MeasurementSystem.Other,
    normalRange = normalRange,
    type = MeasurementType.Other
  )

  // Volume

  //// Metric

  data object Milliliter : Volume(
    name = "milliliter",
    factor = 1.0,
    abbreviation = "mL",
    normalRange = 0.0..<1000.0,
    system = MeasurementSystem.Metric
  )

  data object Liter : Volume(
    name = "liter",
    factor = 1000.0,
    abbreviation = "L",
    normalRange = 0.5..<Double.POSITIVE_INFINITY,
    system = MeasurementSystem.Metric
  )

  //// Imperial

  data object Teaspoon : Volume(
    factor = 4.92892,
    name = "teaspoon",
    abbreviation = "tsp",
    normalRange = 0.0..<3.0,
    system = MeasurementSystem.Imperial
  )

  data object Tablespoon : Volume(
    factor = 14.7868,
    name = "tablespoon",
    abbreviation = "tbsp",
    normalRange = 0.334..<4.0,
    system = MeasurementSystem.Imperial
  )

  data object Cup : Volume(
    factor = 236.588,
    name = "cup",
    abbreviation = "C",
    normalRange = 0.25..<Double.POSITIVE_INFINITY,
    system = MeasurementSystem.Imperial
  )

  data object Pint : Volume(
    factor = 473.176,
    name = "pint",
    abbreviation = "pt",
    normalRange = 0.5..<2.0,
    system = MeasurementSystem.Imperial
  )

  data object Quart : Volume(
    factor = 946.353,
    name = "quart",
    abbreviation = "qt",
    normalRange = 0.5..<4.0,
    system = MeasurementSystem.Imperial
  )

  data object Gallon : Volume(
    factor = 3785.41,
    name = "gallon",
    abbreviation = "gal",
    normalRange = 0.25..<Double.POSITIVE_INFINITY,
    system = MeasurementSystem.Imperial
  )

  data object FluidOunce : Volume(
    factor = 29.5735,
    name = "fluid ounce",
    abbreviation = "fl oz",
    normalRange = 0.5..<8.0,
    system = MeasurementSystem.Imperial
  )

  // Mass

  //// Metric

  data object Gram : Mass(
    factor = 1.0,
    name = "gram",
    abbreviation = "g",
    normalRange = 0.0..<1000.0,
    system = MeasurementSystem.Metric
  )

  data object Kilogram : Mass(
    factor = 1000.0,
    name = "kilogram",
    abbreviation = "kg",
    normalRange = 0.5..<Double.POSITIVE_INFINITY,
    system = MeasurementSystem.Metric
  )

  //// Standard

  data object Ounce : Mass(
    factor = 28.3495,
    name = "ounce",
    abbreviation = "oz",
    normalRange = 0.0..<16.0,
    system = MeasurementSystem.Metric
  )

  data object Pound : Mass(
    factor = 453.592,
    name = "pound",
    abbreviation = "lb",
    normalRange = 0.5..<Double.POSITIVE_INFINITY,
    system = MeasurementSystem.Metric
  )

  companion object {
    val values by lazy {
      listOf(
        Milliliter,
        Liter,
        Teaspoon,
        Tablespoon,
        FluidOunce,
        Cup,
        Pint,
        Quart,
        Gallon,
        Gram,
        Kilogram,
        Ounce,
        Pound,
      ).sortedBy { it.factor }
    }
  }
}