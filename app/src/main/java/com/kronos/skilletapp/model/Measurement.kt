package com.kronos.skilletapp.model

data class Measurement(
  val amount: Double,
  val unit: MeasurementUnit
)

enum class MeasurementType {
  Mass,
  Volume,
  Other
}

sealed class MeasurementUnit(
  val factor: Int,
  val name: String,
  val abbreviation: String,
  val type: MeasurementType
) {
  data object Teaspoon : MeasurementUnit(
    factor = 1,
    name = "teaspoon",
    abbreviation = "tsp",
    type = MeasurementType.Volume
  )

  data object Tablespoon : MeasurementUnit(
    factor = 3,
    name = "tablespoon",
    abbreviation = "tbsp",
    type = MeasurementType.Volume
  )

  data object Cup : MeasurementUnit(
    factor = 48,
    name = "cup",
    abbreviation = "C",
    type = MeasurementType.Volume
  )

  data object Pint : MeasurementUnit(
    factor = 96,
    name = "pint",
    abbreviation = "pt",
    type = MeasurementType.Volume
  )

  data object Quart : MeasurementUnit(
    factor = 192,
    name = "quart",
    abbreviation = "qt",
    type = MeasurementType.Volume
  )

  data object Gallon : MeasurementUnit(
    factor = 768,
    name = "gallon",
    abbreviation = "gal",
    type = MeasurementType.Volume
  )

  data object FluidOunce : MeasurementUnit(
    factor = 6,
    name = "fluid ounce",
    abbreviation = "fl oz",
    type = MeasurementType.Volume
  )
}