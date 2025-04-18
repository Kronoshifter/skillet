package com.kronos.skilletapp.model.measurement

import com.kronos.skilletapp.model.IngredientType.Dry
import com.kronos.skilletapp.model.IngredientType.Wet
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

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

  interface None : MeasurementDimension
}

@Serializable
sealed interface MeasurementSystem {
  interface Metric : MeasurementSystem
  interface UsCustomary : MeasurementSystem
  interface None : MeasurementSystem
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("measurement_type")
sealed interface MeasurementUnit {
  val name: String
  val abbreviation: String
  val aliases: List<String> //TODO: potentially replace aliases here with lookup table
  val normalizationLow: Float
  val normalizationHigh: Float
  val baseUnit: MeasurementUnit

  @Serializable
  @SerialName("mass")
  sealed class Mass(
    override val name: String,
    override val abbreviation: String,
    override val aliases: List<String>,
    override val normalizationLow: Float,
    override val normalizationHigh: Float,
  ) : MeasurementUnit, MeasurementDimension.Mass {
    override val baseUnit: Mass
      get() = Gram
  }

  @Serializable
  @SerialName("volume")
  sealed class Volume(
    override val name: String,
    override val abbreviation: String,
    override val aliases: List<String>,
    override val normalizationLow: Float,
    override val normalizationHigh: Float,
  ) : MeasurementUnit, MeasurementDimension.Volume {
    override val baseUnit: Volume
      get() = Milliliter
  }

  @Serializable
  @SerialName("custom")
  data class Custom(
    override val name: String
  ) : MeasurementUnit, MeasurementSystem.None, MeasurementDimension.None {
    override val abbreviation: String
      get() = name
    override val aliases: List<String>
      get() = listOf(name)
    override val normalizationLow: Float
      get() = 0f
    override val normalizationHigh: Float
      get() = Float.POSITIVE_INFINITY

    override val baseUnit: MeasurementUnit
      get() = this
  }

  @Serializable
  @SerialName("none")
  data object None : MeasurementUnit, MeasurementSystem.None, MeasurementDimension.None {
    override val name: String
      get() = "none"
    override val abbreviation: String
      get() = ""
    override val aliases: List<String>
      get() = emptyList()
    override val normalizationLow: Float
      get() = 0f
    override val normalizationHigh: Float
      get() = Float.POSITIVE_INFINITY

    override val baseUnit: MeasurementUnit
      get() = this
  }

  // Volume

  //// Metric

  @Serializable
  @SerialName("milliliter")
  data object Milliliter : Volume(
    name = "milliliter",
    abbreviation = "mL",
    aliases = listOf("mL"),
    normalizationLow = 0f,
    normalizationHigh = 1000f,
  ), MeasurementSystem.Metric

  @Serializable
  @SerialName("liter")
  data object Liter : Volume(
    name = "liter",
    abbreviation = "L",
    aliases = listOf("L"),
    normalizationLow = 0.51f,
    normalizationHigh = Float.POSITIVE_INFINITY,
  ), MeasurementSystem.Metric

  //// US Customary

  @Serializable
  @SerialName("pinch")
  data object Pinch : Volume(
    name = "pinch",
    abbreviation = "pinch",
    aliases = listOf("pinch"),
    normalizationLow = 0f,
    normalizationHigh = 2f,
  ), MeasurementSystem.UsCustomary

  @Serializable
  @SerialName("dash")
  data object Dash : Volume(
    name = "dash",
    abbreviation = "dash",
    aliases = listOf("dash"),
    normalizationLow = 0.51f,
    normalizationHigh = 2f,
  ), MeasurementSystem.UsCustomary

  @Serializable
  @SerialName("teaspoon")
  data object Teaspoon : Volume(
    name = "teaspoon",
    abbreviation = "tsp",
    aliases = listOf("tsp", "t", "teaspoons"),
    normalizationLow = 0.26f,
    normalizationHigh = 3f,
  ), MeasurementSystem.UsCustomary

  @Serializable
  @SerialName("tablespoon")
  data object Tablespoon : Volume(
    name = "tablespoon",
    abbreviation = "tbsp",
    aliases = listOf("tbsp", "Tbsp", "T", "tbs", "Tbs", "tablespoons", "Tablespoons"),
    normalizationLow = 0.334f,
    normalizationHigh = 4f,
  ), MeasurementSystem.UsCustomary

  @Serializable
  @SerialName("cup")
  data object Cup : Volume(
    name = "cup",
    abbreviation = "cup",
    aliases = listOf("cup", "c", "C", "cups"),
    normalizationLow = 0.26f,
    normalizationHigh = Float.POSITIVE_INFINITY,
  ), MeasurementSystem.UsCustomary

  @Serializable
  @SerialName("pint")
  data object Pint : Volume(
    name = "pint",
    abbreviation = "pt",
    aliases = listOf("pt", "pints", "Pint"),
    normalizationLow = 0.51f,
    normalizationHigh = 2f,
  ), MeasurementSystem.UsCustomary

  @Serializable
  @SerialName("quart")
  data object Quart : Volume(
    name = "quart",
    abbreviation = "qt",
    aliases = listOf("qt", "quarts", "Quart"),
    normalizationLow = 0.51f,
    normalizationHigh = 4f,
  ), MeasurementSystem.UsCustomary

  @Serializable
  @SerialName("gallon")
  data object Gallon : Volume(
    name = "gallon",
    abbreviation = "gal",
    aliases = listOf("gal", "gallons", "Gallon"),
    normalizationLow = 0.26f,
    normalizationHigh = Float.POSITIVE_INFINITY,
  ), MeasurementSystem.UsCustomary

  @Serializable
  @SerialName("fluid_ounce")
  data object FluidOunce : Volume(
    name = "fluid ounce",
    abbreviation = "fl oz",
    aliases = listOf("fl oz"),
    normalizationLow = 0.51f,
    normalizationHigh = 8f,
  ), MeasurementSystem.UsCustomary

  // Mass

  //// Metric

  @Serializable
  @SerialName("gram")
  data object Gram : Mass(
    name = "gram",
    abbreviation = "g",
    aliases = listOf("g", "grams"),
    normalizationLow = 0f,
    normalizationHigh = 1000f,
  ), MeasurementSystem.Metric

  @Serializable
  @SerialName("kilogram")
  data object Kilogram : Mass(
    name = "kilogram",
    abbreviation = "kg",
    aliases = listOf("kg", "kilograms"),
    normalizationLow = 0.51f,
    normalizationHigh = Float.POSITIVE_INFINITY,
  ), MeasurementSystem.Metric

  //// Standard

  @Serializable
  @SerialName("ounce")
  data object Ounce : Mass(
    name = "ounce",
    abbreviation = "oz",
    aliases = listOf("oz", "ounces", "Ounce"),
    normalizationLow = 0f,
    normalizationHigh = 16f,
  ), MeasurementSystem.UsCustomary

  @Serializable
  @SerialName("pound")
  data object Pound : Mass(
    name = "pound",
    abbreviation = "lb",
    aliases = listOf("lb", "lbs", "pounds", "Pound"),
    normalizationLow = 0.51f,
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
          { (1 of it).convertToBaseUnit().quantity },
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