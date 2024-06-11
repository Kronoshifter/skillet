package com.kronos.skilletapp.model

data class Measurement(
  val amount: Double,
  val unit: MeasurementUnit,
) {
  fun scale(factor: Double) = Measurement(amount * factor, unit)

  fun convertTo(to: MeasurementUnit): Measurement {
    val factor = to.factor / unit.factor
    return Measurement(amount * factor, to)
  }

}

enum class MeasurementType {
  Mass,
  Volume,
  Other
}

sealed class MeasurementUnit(
  open val name: String,
  val factor: Double,
  val abbreviation: String,
  val type: MeasurementType,
) {

  // Volume

  //// Metric

  data object Milliliter : MeasurementUnit(
    name = "milliliter",
    factor = 1.0,
    abbreviation = "mL",
    type = MeasurementType.Volume
  )

  data object Liter : MeasurementUnit(
    name = "liter",
    factor = 1000.0,
    abbreviation = "L",
    type = MeasurementType.Volume
  )

  //// Standard

  data object Teaspoon : MeasurementUnit(
    factor = 4.929,
    name = "teaspoon",
    abbreviation = "tsp",
    type = MeasurementType.Volume
  )

  data object Tablespoon : MeasurementUnit(
    factor = 14.789,
    name = "tablespoon",
    abbreviation = "tbsp",
    type = MeasurementType.Volume
  )

  data object Cup : MeasurementUnit(
    factor = 240.0,
    name = "cup",
    abbreviation = "C",
    type = MeasurementType.Volume
  )

  data object Pint : MeasurementUnit(
    factor = 473.176,
    name = "pint",
    abbreviation = "pt",
    type = MeasurementType.Volume
  )

  data object Quart : MeasurementUnit(
    factor = 946.353,
    name = "quart",
    abbreviation = "qt",
    type = MeasurementType.Volume
  )

  data object Gallon : MeasurementUnit(
    factor = 3785.410,
    name = "gallon",
    abbreviation = "gal",
    type = MeasurementType.Volume
  )

  data object FluidOunce : MeasurementUnit(
    factor = 29.574,
    name = "fluid ounce",
    abbreviation = "fl oz",
    type = MeasurementType.Volume
  )

  // Mass

  //// Metric

  data object Gram : MeasurementUnit(
    factor = 1.0,
    name = "gram",
    abbreviation = "g",
    type = MeasurementType.Mass
  )

  data object Kilogram : MeasurementUnit(
    factor = 1000.0,
    name = "kilogram",
    abbreviation = "kg",
    type = MeasurementType.Mass
  )

  //// Standard

  data object Ounce : MeasurementUnit(
    factor = 28.350,
    name = "ounce",
    abbreviation = "oz",
    type = MeasurementType.Mass
  )

  data object Pound : MeasurementUnit(
    factor = 453.592,
    name = "pound",
    abbreviation = "lb",
    type = MeasurementType.Mass
  )

  // Other

  data class Custom(override val name: String) : MeasurementUnit(
    factor = 1.0,
    name = name,
    abbreviation = name,
    type = MeasurementType.Other
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