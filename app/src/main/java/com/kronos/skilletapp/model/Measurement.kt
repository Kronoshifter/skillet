package com.kronos.skilletapp.model

import com.github.michaelbull.result.*
import com.kronos.skilletapp.utils.roundToEighth
import kotlin.math.roundToInt
import com.kronos.skilletapp.model.IngredientType.*

data class Measurement(
  val quantity: Double,
  val unit: MeasurementUnit,
) {
  operator fun times(factor: Double) = scale(factor)
  operator fun times(factor: Int) = scale(factor.toDouble())

  operator fun div(factor: Double) = scale(1 / factor)
  operator fun div(factor: Int) = scale(1 / factor.toDouble())

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

  fun scale(factor: Double) = copy(quantity = quantity * factor)

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

  fun convert(to: MeasurementUnit, converter: (Double) -> Double) = Measurement(converter(quantity), unit = to)
  

  fun scaleAndNormalize(factor: Double) = scale(factor).normalize()

  fun normalize(filter: ((MeasurementUnit) -> Boolean)? = null): Measurement {
    var normalized = copy()
    while (normalized.quantity !in normalized.unit.normalizationLow..<normalized.unit.normalizationHigh) {
      if (normalized.quantity <= normalized.unit.normalizationLow) {
        normalized = normalized.convert(normalized.unit.previous(filter).unwrap())
      } else if (normalized.quantity >= normalized.unit.normalizationHigh) {
        normalized = normalized.convert(normalized.unit.next(filter).unwrap())
      }
    }
    return normalized
  }

  fun roundToEighth() = copy(quantity = quantity.roundToEighth())
  fun round() = copy(quantity = quantity.roundToInt().toDouble())

  fun scaleAndRound(factor: Double) = scale(factor).roundToEighth()
}

fun MeasurementUnit.next(filter: ((MeasurementUnit) -> Boolean)? = null): Result<MeasurementUnit, Unit> {
  val filtered = MeasurementUnit.values.filter { it.type == this.type }.filter { it.system == this.system }.filter { filter?.invoke(it) ?: true }
  return filtered.getOrNull(filtered.indexOf(this) + 1).toResultOr {  }
}

fun MeasurementUnit.previous(filter: ((MeasurementUnit) -> Boolean)? = null): Result<MeasurementUnit, Unit> {
  val filtered = MeasurementUnit.values.filter { it.type == this.type }.filter { it.system == this.system }.filter { filter?.invoke(it) ?: true }
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
  open val normalizationLow: Double,
  open val normalizationHigh: Double,
  open val type: MeasurementType,
) {

  sealed class Mass(
    override val name: String,
    override val factor: Double,
    override val abbreviation: String,
    override val normalizationLow: Double,
    override val normalizationHigh: Double,
    override val system: MeasurementSystem,
  ) : MeasurementUnit(
    name = name,
    factor = factor,
    abbreviation = abbreviation,
    system = system,
    normalizationLow = normalizationLow,
    normalizationHigh = normalizationHigh,
    type = MeasurementType.Mass)

  sealed class Volume(
    override val name: String,
    override val factor: Double,
    override val abbreviation: String,
    override val normalizationLow: Double,
    override val normalizationHigh: Double,
    override val system: MeasurementSystem,
  ) : MeasurementUnit(
    name = name,
    factor = factor,
    abbreviation = abbreviation,
    system = system,
    normalizationLow = normalizationLow,
    normalizationHigh = normalizationHigh,
    type = MeasurementType.Volume
  )

  data class Custom(
    override val name: String,
    override val abbreviation: String,
  ) : MeasurementUnit(
    name = name,
    factor = 1.0,
    abbreviation = abbreviation,
    system = MeasurementSystem.Other,
    normalizationLow = 0.0,
    normalizationHigh = Double.POSITIVE_INFINITY,
    type = MeasurementType.Other
  )

  // Volume

  //// Metric

  data object Milliliter : Volume(
    name = "milliliter",
    factor = 1.0,
    abbreviation = "mL",
    normalizationLow = 0.0,
    normalizationHigh = 1000.0,
    system = MeasurementSystem.Metric
  )

  data object Liter : Volume(
    name = "liter",
    factor = 1000.0,
    abbreviation = "L",
    normalizationLow = 0.5,
    normalizationHigh = Double.POSITIVE_INFINITY,
    system = MeasurementSystem.Metric
  )

  //// Imperial

  data object Pinch : Volume(
    factor = 0.3080575,
    name = "pinch",
    abbreviation = "pinch",
    normalizationLow = 0.0,
    normalizationHigh = 2.0,
    system = MeasurementSystem.Imperial
  )

  data object Dash : Volume(
    factor = 0.616115,
    name = "dash",
    abbreviation = "dash",
    normalizationLow = 0.5,
    normalizationHigh = 2.0,
    system = MeasurementSystem.Imperial
  )

  data object Teaspoon : Volume(
    factor = 4.92892,
    name = "teaspoon",
    abbreviation = "tsp",
    normalizationLow = 0.25,
    normalizationHigh = 3.0,
    system = MeasurementSystem.Imperial
  )

  data object Tablespoon : Volume(
    factor = 14.7868,
    name = "tablespoon",
    abbreviation = "tbsp",
    normalizationLow = 0.334,
    normalizationHigh = 4.0,
    system = MeasurementSystem.Imperial
  )

  data object Cup : Volume(
    factor = 236.588,
    name = "cup",
    abbreviation = "cup",
    normalizationLow = 0.25,
    normalizationHigh = Double.POSITIVE_INFINITY,
    system = MeasurementSystem.Imperial
  )

  data object Pint : Volume(
    factor = 473.176,
    name = "pint",
    abbreviation = "pt",
    normalizationLow = 0.5,
    normalizationHigh = 2.0,
    system = MeasurementSystem.Imperial
  )

  data object Quart : Volume(
    factor = 946.353,
    name = "quart",
    abbreviation = "qt",
    normalizationLow = 0.5,
    normalizationHigh = 4.0,
    system = MeasurementSystem.Imperial
  )

  data object Gallon : Volume(
    factor = 3785.41,
    name = "gallon",
    abbreviation = "gal",
    normalizationLow = 0.25,
    normalizationHigh = Double.POSITIVE_INFINITY,
    system = MeasurementSystem.Imperial
  )

  data object FluidOunce : Volume(
    factor = 29.5735,
    name = "fluid ounce",
    abbreviation = "fl oz",
    normalizationLow = 0.5,
    normalizationHigh = 8.0,
    system = MeasurementSystem.Imperial
  )

  // Mass

  //// Metric

  data object Gram : Mass(
    factor = 1.0,
    name = "gram",
    abbreviation = "g",
    normalizationLow = 0.0,
    normalizationHigh = 1000.0,
    system = MeasurementSystem.Metric
  )

  data object Kilogram : Mass(
    factor = 1000.0,
    name = "kilogram",
    abbreviation = "kg",
    normalizationLow = 0.5,
    normalizationHigh = Double.POSITIVE_INFINITY,
    system = MeasurementSystem.Metric
  )

  //// Standard

  data object Ounce : Mass(
    factor = 28.3495,
    name = "ounce",
    abbreviation = "oz",
    normalizationLow = 0.0,
    normalizationHigh = 16.0,
    system = MeasurementSystem.Imperial
  )

  data object Pound : Mass(
    factor = 453.592,
    name = "pound",
    abbreviation = "lb",
    normalizationLow = 0.5,
    normalizationHigh = Double.POSITIVE_INFINITY,
    system = MeasurementSystem.Imperial
  )

  companion object {
    val values by lazy {
      listOf(
        Milliliter,
        Liter,
        Pinch,
        Dash,
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

    private val wet = listOf(Wet)
    private val dry = listOf(Dry)
    private val either = listOf(Wet, Dry)

    val allowedIngredientTypes = mapOf(
      Milliliter to either,
      Liter to either,
      Pinch to dry,
      Dash to wet,
      Teaspoon to either,
      Tablespoon to either,
      FluidOunce to wet,
      Cup to either,
      Pint to either,
      Quart to either,
      Gallon to either,
      Gram to either,
      Kilogram to either,
      Ounce to either,
      Pound to either,
    )
  }
}