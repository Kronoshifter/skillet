package com.kronos.skilletapp.model.measurement

import com.kronos.skilletapp.utils.mutateUnless

class MeasurementConverter(
  val ratio: MeasurementRatio
) {
  infix fun convert(quantity: Float): Measurement = convert(quantity, ratio)
  infix fun reverse(quantity: Float): Measurement = convert(quantity, ratio.invert())

  private fun convert(quantity: Float, ratio: MeasurementRatio) = ratio.run { Measurement(quantity * decimal, right.unit) }

  val from get() = ratio.left.unit
  val to get() = ratio.right.unit

  @MeasurementUnitConverterDsl
  class Builder {
    private var ratio: MeasurementRatio = MeasurementRatio.None

    fun build(): MeasurementConverter = MeasurementConverter(ratio)

    fun ratio(builder: RatioBuilder.() -> Unit) {
      ratio = RatioBuilder().apply(builder).build()
    }

    infix fun Measurement.to(other: Measurement) = ratio {
      quantity {
        left = this@to
        right = other
      }
    }

    infix fun MeasurementUnit.to(other: MeasurementUnit) = ratio {
      unit {
        left = 1 of this@to
        right = 1 of other
      }
    }
  }

  companion object {
    val baseConverters = listOf(
      MeasurementUnit.Liter.baseConverter(1000),
      MeasurementUnit.Pinch.baseConverter(0.3080575),
      MeasurementUnit.Dash.baseConverter(0.616115),
      MeasurementUnit.Teaspoon.baseConverter(4.92892),
      MeasurementUnit.Tablespoon.baseConverter(14.7868),
      MeasurementUnit.FluidOunce.baseConverter(29.5735),
      MeasurementUnit.Cup.baseConverter(236.588),
      MeasurementUnit.Pint.baseConverter(473.176),
      MeasurementUnit.Quart.baseConverter(946.353),
      MeasurementUnit.Gallon.baseConverter(3785.41),
      MeasurementUnit.Kilogram.baseConverter(1000),
      MeasurementUnit.Ounce.baseConverter(28.3495),
      MeasurementUnit.Pound.baseConverter(453.592),
    )
  }
}

private fun MeasurementUnit.baseConverter(baseUnitQuantity: Number): MeasurementConverter = converter { (1 of this@baseConverter) to (baseUnitQuantity of baseUnit) }

fun converter(builder: MeasurementConverter.Builder.() -> Unit): MeasurementConverter = MeasurementConverter.Builder().apply(builder).build()

@MeasurementUnitConverterDsl
infix fun MeasurementConverter.convert(block: context(MeasurementConversionScope, MeasurementConverter) () -> Measurement): Measurement = MeasurementConversionScope.run { block() }

@MeasurementUnitConverterDsl
fun convert(block: context(MeasurementConversionScope) () -> MeasurementUnitConversion): MeasurementUnitConversion = MeasurementConversionScope.run { block() }

@MeasurementUnitConverterDsl
infix fun MeasurementUnitConversion.using(builder: MeasurementConverter.Builder.() -> Unit): Measurement = converter { builder() } convert { from convertTo to }

infix fun Measurement.convertTo(to: MeasurementUnit): Measurement {
  require(unit hasSameDimensionAs to) {
    "This overload is only valid if the units have the same dimension, try using the overload that takes a measurement"
  }

  if (to.isBaseUnit()) return convertToBaseUnit()

  return convert { this to to } using { unit to to }
}

infix fun Measurement.convertTo(to: Measurement): Measurement {
  val converter = if (unit hasSameDimensionAs to.unit) {
    converter { unit to to.unit }
  } else {
    converter { this@convertTo to to }
  }

  return converter convert {
    this@convertTo convertTo to.unit
  }
}

infix fun Measurement.convertBy(block: MeasurementConverter.Builder.() -> Unit): Measurement {
  return converter(block).let { converter ->
    converter convert {
      this@convertBy convertTo converter.ratio.right.unit
    }
  }
}

fun Measurement.convertToBaseUnit(): Measurement {
  if (unit.isBaseUnit()) return this

  val converter = requireNotNull(MeasurementConverter.baseConverters.find { it.from == unit }) {
    "No base unit converter found for ${unit.name}"
  }

  return converter convert {
    this@convertToBaseUnit convertTo unit.baseUnit
  }
}

object MeasurementConversionScope

@MeasurementUnitConverterDsl
context(_: MeasurementConversionScope, converter: MeasurementConverter)
infix fun Measurement.convertTo(to: MeasurementUnit): Measurement {
  val left = converter.ratio.left.unit
  val right = converter.ratio.right.unit

  require(unit hasSameDimensionAs left) {
    """
        First unit in converter block must measure the same dimension as the measurement to be converted, if necessary chain calls
        Measurement to be converted: $this
        First measurement: ${converter.ratio.left}
    """.trimIndent()
  }

  require(to hasSameDimensionAs right) {
    """
        Second unit in converter block must measure the same dimension unit to be converted to, if necessary chain calls
        Unit to be converted to: $this
        Second measurement: ${converter.ratio.right}
    """.trimIndent()
  }

  return if (unit == left && to == right) {
    this convertWith converter
  } else {
    this convertTo left convertWith converter convertTo to
  }
}

context(_: MeasurementConversionScope)
private infix fun Measurement.convertWith(converter: MeasurementConverter) = converter.convert(quantity)

context(_: MeasurementConversionScope)
private infix fun Measurement.reverseWith(converter: MeasurementConverter) = converter.reverse(quantity)

data class MeasurementUnitConversion(val from: Measurement, val to: MeasurementUnit)

context(_: MeasurementConversionScope)
infix fun Measurement.to(to: MeasurementUnit): MeasurementUnitConversion = MeasurementUnitConversion(this, to)

@MeasurementUnitConverterDsl
class RatioBuilder {
  var left: Measurement = Measurement.None
  var right: Measurement = Measurement.None

  private var ratio: MeasurementRatio = MeasurementRatio.None

  fun unit(block: RatioBuilder.() -> Unit = {}) {
    block()
    check(left.unit hasSameDimensionAs right.unit) { "Units must have the same dimension" }
    ratio = MeasurementRatio.Unit(left, right)
  }

  fun quantity(block: RatioBuilder.() -> Unit = {}) {
    block()
    ratio = MeasurementRatio.Quantity(left, right)
  }

  fun build(): MeasurementRatio {
    check(ratio != MeasurementRatio.None) { "Ratio must be initialized, did you call ratio with an empty block?" }
    return ratio
  }
}

sealed interface MeasurementRatio {
  val decimal: Float
  val left: Measurement
  val right: Measurement

  fun invert(): MeasurementRatio
  fun checkMeasurementsAreSet() = check(left.isNotNone() && right.isNotNone()) { "Measurements must be initialized before getting the ratio" }

  data class Unit(
    override val left: Measurement = Measurement.None,
    override val right: Measurement = Measurement.None
  ) : MeasurementRatio {
    override val decimal: Float
      get() {
        checkMeasurementsAreSet()
        val leftInBase = left.mutateUnless(left.unit.isBaseUnit()) { left.convertToBaseUnit() }
        val rightInBase = right.mutateUnless(right.unit.isBaseUnit()) { right.convertToBaseUnit() }

        return leftInBase.quantity / rightInBase.quantity
      }

    override fun invert() = copy(left = right, right = left)
  }

  data class Quantity(
    override val left: Measurement = Measurement.None,
    override val right: Measurement = Measurement.None
  ) : MeasurementRatio {
    override val decimal: Float
      get() {
        checkMeasurementsAreSet()
        return right.quantity / left.quantity
      }

    override fun invert() = copy(left = right, right = left)
  }

  object None : MeasurementRatio {
    override val decimal: Float
      get() = 0f
    override val left: Measurement
      get() = Measurement.None
    override val right: Measurement
      get() = Measurement.None

    override fun invert() = None
  }
}

@DslMarker
annotation class MeasurementUnitConverterDsl