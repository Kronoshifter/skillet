package com.kronos.skilletapp.model

data class Measurement(
  val amount: Double,
  val unit: MeasurementUnit,
) {
  fun scale(factor: Double) = copy(amount = amount * factor)

  fun convert(to: MeasurementUnit.Mass): Measurement {
    check(unit is MeasurementUnit.Mass)

    val factor = to.factor / unit.factor
    return Measurement(amount = amount * factor, unit = to)
  }

  fun convert(to: MeasurementUnit.Volume): Measurement {
    check(unit is MeasurementUnit.Volume)

    val factor = to.factor / unit.factor
    return Measurement(amount = amount * factor, unit = to)
  }

  fun convert(to: MeasurementUnit, conversion: () -> Double): Measurement {
    val factor = conversion()
    return Measurement(amount = amount * factor, unit = to)
  }
}

enum class MeasurementType {
  Mass,
  Volume,
  Other
}

sealed class MeasurementUnit(
  open val name: String,
  open val factor: Double,
  open val abbreviation: String,
//  val type: MeasurementType,
) {

  sealed class Mass(
    override val name: String,
    override val factor: Double,
    override val abbreviation: String,
  ) : MeasurementUnit(name, factor, abbreviation)

  sealed class Volume(
    override val name: String,
    override val factor: Double,
    override val abbreviation: String,
  ) : MeasurementUnit(name, factor, abbreviation)

  data class Custom(
    override val name: String,
    override val factor: Double,
    override val abbreviation: String,
  ) : MeasurementUnit(name, factor, abbreviation)

  // Volume

  //// Metric

  data object Milliliter : Volume(
    name = "milliliter",
    factor = 1.0,
    abbreviation = "mL",
  )

  data object Liter : Volume(
    name = "liter",
    factor = 1000.0,
    abbreviation = "L",
  )

  //// Standard

  data object Teaspoon : Volume(
    factor = 4.929,
    name = "teaspoon",
    abbreviation = "tsp",
  )

  data object Tablespoon : Volume(
    factor = 14.789,
    name = "tablespoon",
    abbreviation = "tbsp",
  )

  data object Cup : Volume(
    factor = 240.0,
    name = "cup",
    abbreviation = "C",
  )

  data object Pint : Volume(
    factor = 473.176,
    name = "pint",
    abbreviation = "pt",
  )

  data object Quart : Volume(
    factor = 946.353,
    name = "quart",
    abbreviation = "qt",
  )

  data object Gallon : Volume(
    factor = 3785.410,
    name = "gallon",
    abbreviation = "gal",
  )

  data object FluidOunce : Volume(
    factor = 29.574,
    name = "fluid ounce",
    abbreviation = "fl oz",
  )

  // Mass

  //// Metric

  data object Gram : Mass(
    factor = 1.0,
    name = "gram",
    abbreviation = "g",
  )

  data object Kilogram : Mass(
    factor = 1000.0,
    name = "kilogram",
    abbreviation = "kg",
  )

  //// Standard

  data object Ounce : Mass(
    factor = 28.350,
    name = "ounce",
    abbreviation = "oz",
  )

  data object Pound : Mass(
    factor = 453.592,
    name = "pound",
    abbreviation = "lb",
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
  }
}