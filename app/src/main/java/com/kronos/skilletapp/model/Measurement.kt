package com.kronos.skilletapp.model

data class Measurement(
  val amount: Double,
  val unit: MeasurementUnit,
) {
  fun scale(factor: Double) = copy(amount = amount * factor)
  operator fun times(factor: Double) = scale(factor)
  operator fun times(factor: Int) = scale(factor.toDouble())

  fun convert(to: MeasurementUnit.Mass) = convert(to) {
    check(unit is MeasurementUnit.Mass)
    it * unit.factor / to.factor
  }

  fun convert(to: MeasurementUnit.Volume) = convert(to) {
    check(unit is MeasurementUnit.Volume)
    it * unit.factor / to.factor
  }

  fun convert(to: MeasurementUnit, converter: (Double) -> Double) = Measurement(converter(amount), unit = to)
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
//    fun values() = MeasurementUnit::class.sealedSubclasses.mapNotNull { it.objectInstance as? MeasurementUnit }

    fun values() = listOf(
      Milliliter,
      Liter,
      Teaspoon,
      Tablespoon,
      Cup,
      Pint,
      Quart,
      Gallon,
      FluidOunce,
      Gram,
      Kilogram,
      Ounce,
      Pound,
    )

    fun fromName(name: String) = values().firstOrNull { it.name == name }
    fun fromAbbreviation(abbreviation: String) = values().firstOrNull { it.abbreviation == abbreviation }

    fun byType(type: MeasurementType) = values().filter { it.type == type }
    fun bySystem(system: MeasurementSystem) = values().filter { it.system == system }
  }
}