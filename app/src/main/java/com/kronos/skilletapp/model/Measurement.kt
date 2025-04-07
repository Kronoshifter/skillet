package com.kronos.skilletapp.model

import com.github.michaelbull.result.*
import com.kronos.skilletapp.utils.roundToEighth
import kotlin.math.roundToInt
import com.kronos.skilletapp.model.IngredientType.*
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

@Serializable
sealed interface MeasurementDimension {
  val baseUnit: MeasurementUnit

  interface Mass : MeasurementDimension {
    override val baseUnit: MeasurementUnit.Mass
      get() = MeasurementUnit.Gram
  }

  interface Volume : MeasurementDimension {
    override val baseUnit: MeasurementUnit.Volume
      get() = MeasurementUnit.Milliliter
  }

  interface None : MeasurementDimension {
    override val baseUnit: MeasurementUnit.None
      get() = MeasurementUnit.None
  }
}

@Serializable
sealed interface MeasurementSystem {
  interface Metric : MeasurementSystem
  interface UsCustomary : MeasurementSystem
  interface Custom : MeasurementSystem
  interface None : MeasurementSystem
}

@Serializable
sealed interface MeasurementUnit {
  val name: String
  val factor: Float
  val abbreviation: String
  val aliases: List<String> //TODO: potentially replace aliases here with lookup table
  val normalizationLow: Float
  val normalizationHigh: Float

  @Serializable
  sealed class Mass(
    override val name: String,
    override val factor: Float,
    override val abbreviation: String,
    override val aliases: List<String>,
    override val normalizationLow: Float,
    override val normalizationHigh: Float,
  ) : MeasurementUnit, MeasurementDimension.Mass

  @Serializable
  sealed class Volume(
    override val name: String,
    override val factor: Float,
    override val abbreviation: String,
    override val aliases: List<String>,
    override val normalizationLow: Float,
    override val normalizationHigh: Float,
  ) : MeasurementUnit, MeasurementDimension.Volume

  @Serializable
  data class Custom(
    override val name: String
  ) : MeasurementUnit, MeasurementSystem.Custom, MeasurementDimension.None {
    override val factor: Float
      get() = 1f
    override val abbreviation: String
      get() = name
    override val aliases: List<String>
      get() = listOf(name)
    override val normalizationLow: Float
      get() = 0f
    override val normalizationHigh: Float
      get() = Float.POSITIVE_INFINITY
  }

  @Serializable
  data object None : MeasurementUnit, MeasurementSystem.None, MeasurementDimension.None {
    override val name: String
      get() = "none"
    override val factor: Float
      get() = 1f
    override val abbreviation: String
      get() = "none"
    override val aliases: List<String>
      get() = listOf("none")
    override val normalizationLow: Float
      get() = 0f
    override val normalizationHigh: Float
      get() = Float.POSITIVE_INFINITY
  }

  // Volume

  //// Metric

  @Serializable
  data object Milliliter : Volume(
    name = "milliliter",
    factor = 1f,
    abbreviation = "mL",
    aliases = listOf("mL"),
    normalizationLow = 0f,
    normalizationHigh = 1000f,
  ), MeasurementSystem.Metric

  @Serializable
  data object Liter : Volume(
    name = "liter",
    factor = 1000f,
    abbreviation = "L",
    aliases = listOf("L"),
    normalizationLow = 0.5f,
    normalizationHigh = Float.POSITIVE_INFINITY,
  ), MeasurementSystem.Metric

  //// US Customary

  @Serializable
  data object Pinch : Volume(
    factor = 0.3080575f,
    name = "pinch",
    abbreviation = "pinch",
    aliases = listOf("pinch"),
    normalizationLow = 0f,
    normalizationHigh = 2f,
  ), MeasurementSystem.UsCustomary

  @Serializable
  data object Dash : Volume(
    factor = 0.616115f,
    name = "dash",
    abbreviation = "dash",
    aliases = listOf("dash"),
    normalizationLow = 0.5f,
    normalizationHigh = 2f,
  ), MeasurementSystem.UsCustomary

  @Serializable
  data object Teaspoon : Volume(
    factor = 4.92892f,
    name = "teaspoon",
    abbreviation = "tsp",
    aliases = listOf("tsp", "t", "teaspoons"),
    normalizationLow = 0.25f,
    normalizationHigh = 3f,
  ), MeasurementSystem.UsCustomary

  @Serializable
  data object Tablespoon : Volume(
    factor = 14.7868f,
    name = "tablespoon",
    abbreviation = "tbsp",
    aliases = listOf("tbsp", "Tbsp", "T", "tbs", "Tbs", "tablespoons", "Tablespoons"),
    normalizationLow = 0.334f,
    normalizationHigh = 4f,
  ), MeasurementSystem.UsCustomary

  @Serializable
  data object Cup : Volume(
    factor = 236.588f,
    name = "cup",
    abbreviation = "cup",
    aliases = listOf("cup", "c", "C", "cups"),
    normalizationLow = 0.25f,
    normalizationHigh = Float.POSITIVE_INFINITY,
  ), MeasurementSystem.UsCustomary

  @Serializable
  data object Pint : Volume(
    factor = 473.176f,
    name = "pint",
    abbreviation = "pt",
    aliases = listOf("pt", "pints", "Pint"),
    normalizationLow = 0.5f,
    normalizationHigh = 2f,
  ), MeasurementSystem.UsCustomary

  @Serializable
  data object Quart : Volume(
    factor = 946.353f,
    name = "quart",
    abbreviation = "qt",
    aliases = listOf("qt", "quarts", "Quart"),
    normalizationLow = 0.5f,
    normalizationHigh = 4f,
  ), MeasurementSystem.UsCustomary

  @Serializable
  data object Gallon : Volume(
    factor = 3785.41f,
    name = "gallon",
    abbreviation = "gal",
    aliases = listOf("gal", "gallons", "Gallon"),
    normalizationLow = 0.25f,
    normalizationHigh = Float.POSITIVE_INFINITY,
  ), MeasurementSystem.UsCustomary

  @Serializable
  data object FluidOunce : Volume(
    factor = 29.5735f,
    name = "fluid ounce",
    abbreviation = "fl oz",
    aliases = listOf("fl oz"),
    normalizationLow = 0.5f,
    normalizationHigh = 8f,
  ), MeasurementSystem.UsCustomary

  // Mass

  //// Metric

  @Serializable
  data object Gram : Mass(
    factor = 1f,
    name = "gram",
    abbreviation = "g",
    aliases = listOf("g", "grams"),
    normalizationLow = 0f,
    normalizationHigh = 1000f,
  ), MeasurementSystem.Metric

  @Serializable
  data object Kilogram : Mass(
    factor = 1000f,
    name = "kilogram",
    abbreviation = "kg",
    aliases = listOf("kg", "kilograms"),
    normalizationLow = 0.5f,
    normalizationHigh = Float.POSITIVE_INFINITY,
  ), MeasurementSystem.Metric

  //// Standard

  @Serializable
  data object Ounce : Mass(
    factor = 28.3495f,
    name = "ounce",
    abbreviation = "oz",
    aliases = listOf("oz", "ounces", "Ounce"),
    normalizationLow = 0f,
    normalizationHigh = 16f,
  ), MeasurementSystem.UsCustomary

  @Serializable
  data object Pound : Mass(
    factor = 453.592f,
    name = "pound",
    abbreviation = "lb",
    aliases = listOf("lb", "lbs", "pounds", "Pound"),
    normalizationLow = 0.5f,
    normalizationHigh = Float.POSITIVE_INFINITY,
  ), MeasurementSystem.UsCustomary

  companion object {
    val values: List<MeasurementUnit> by lazy {
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
      ).sortedWith(
        compareBy(
          { it.factor },
          {
            when (it) {
              is Volume -> 0
              is Mass -> 1
              is Custom -> 2
              is None -> 3
            }
          }
        )
      )
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

    fun fromName(unit: String?) = unit?.let {
      values.firstOrNull {
        it.name == unit || it.abbreviation == unit || it.aliases.contains(unit)
      } ?: Custom(unit)
    } ?: None
  }
}