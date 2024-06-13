package com.kronos.skilletapp.model

import com.github.michaelbull.result.*
import com.kronos.skilletapp.utils.roundToEighth
import kotlin.math.round

data class Measurement(
  val amount: Double,
  val unit: MeasurementUnit,
) {
  fun scale(factor: Double) = copy(amount = amount * factor)
  operator fun times(factor: Double) = scale(factor)
  operator fun times(factor: Int) = scale(factor.toDouble())

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

  fun scaleAndConvert(factor: Double): Measurement {
    var scaled = scale(factor)

    do {
      // TODO: I don't know if this is the best way to calculate conversion thresholds. Hardcoded values might be better
      val scaledAmount = scaled.amount.roundToEighth()
      val low = scaled.unit.previous().mapOr(0.0) { it.factor / scaled.unit.factor }.roundToEighth()
      val high = scaled.unit.next().mapOr(Double.POSITIVE_INFINITY) { it.factor / scaled.unit.factor }.roundToEighth()

      if (scaledAmount <= low) {
        scaled = scaled.convert(scaled.unit.previous().unwrap())
      } else if (scaledAmount >= high) {
        scaled = scaled.convert(scaled.unit.next().unwrap())
      }
    } while (scaledAmount !in low..high)

    return scaled
  }
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
  open val type: MeasurementType,
) {

  sealed class Mass(
    override val name: String,
    override val factor: Double,
    override val abbreviation: String,
    override val system: MeasurementSystem,
  ) : MeasurementUnit(name, factor, abbreviation, system, type = MeasurementType.Mass)

  sealed class Volume(
    override val name: String,
    override val factor: Double,
    override val abbreviation: String,
    override val system: MeasurementSystem,
  ) : MeasurementUnit(name, factor, abbreviation, system, type = MeasurementType.Volume)

  data class Custom(
    override val name: String,
    override val factor: Double,
    override val abbreviation: String,
  ) : MeasurementUnit(name, factor, abbreviation, system = MeasurementSystem.Other, type = MeasurementType.Other)

  // Volume

  //// Metric

  data object Milliliter : Volume(
    name = "milliliter",
    factor = 1.0,
    abbreviation = "mL",
    system = MeasurementSystem.Metric
  )

  data object Liter : Volume(
    name = "liter",
    factor = 1000.0,
    abbreviation = "L",
    system = MeasurementSystem.Metric
  )

  //// Imperial

  data object Teaspoon : Volume(
    factor = 4.92892,
    name = "teaspoon",
    abbreviation = "tsp",
    system = MeasurementSystem.Imperial
  )

  data object Tablespoon : Volume(
    factor = 14.7868,
    name = "tablespoon",
    abbreviation = "tbsp",
    system = MeasurementSystem.Imperial
  )

  data object Cup : Volume(
    factor = 236.588,
    name = "cup",
    abbreviation = "C",
    system = MeasurementSystem.Imperial
  )

  data object Pint : Volume(
    factor = 473.176,
    name = "pint",
    abbreviation = "pt",
    system = MeasurementSystem.Imperial
  )

  data object Quart : Volume(
    factor = 946.353,
    name = "quart",
    abbreviation = "qt",
    system = MeasurementSystem.Imperial
  )

  data object Gallon : Volume(
    factor = 3785.41,
    name = "gallon",
    abbreviation = "gal",
    system = MeasurementSystem.Imperial
  )

  data object FluidOunce : Volume(
    factor = 29.5735,
    name = "fluid ounce",
    abbreviation = "fl oz",
    system = MeasurementSystem.Imperial
  )

  // Mass

  //// Metric

  data object Gram : Mass(
    factor = 1.0,
    name = "gram",
    abbreviation = "g",
    system = MeasurementSystem.Metric
  )

  data object Kilogram : Mass(
    factor = 1000.0,
    name = "kilogram",
    abbreviation = "kg",
    system = MeasurementSystem.Metric
  )

  //// Standard

  data object Ounce : Mass(
    factor = 28.3495,
    name = "ounce",
    abbreviation = "oz",
    system = MeasurementSystem.Metric
  )

  data object Pound : Mass(
    factor = 453.592,
    name = "pound",
    abbreviation = "lb",
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