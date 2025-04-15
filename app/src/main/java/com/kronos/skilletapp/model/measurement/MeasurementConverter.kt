package com.kronos.skilletapp.model.measurement

import com.kronos.skilletapp.model.measurement.MeasurementUnit.None.baseUnit
import com.kronos.skilletapp.utils.mutateIf
import com.kronos.skilletapp.utils.mutateUnless
import kotlin.div

class MeasurementConverter(
  val ratio: MeasurementRatio
) {
  fun convert(quantity: Float): Measurement = convert(quantity, ratio)
  fun reverse(quantity: Float): Measurement = convert(quantity, ratio.invert())

  private fun convert(quantity: Float, ratio: MeasurementRatio) = with(ratio) { Measurement(quantity * decimal, right.unit) }

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

fun MeasurementUnit.isBaseUnit(): Boolean = this == baseUnit

fun converter(builder: MeasurementConverter.Builder.() -> Unit): MeasurementConverter {
  return MeasurementConverter.Builder().apply(builder).build()
}

fun withConverter(converter: MeasurementConverter, block: MeasurementConversionScope.() -> Measurement): Measurement {
  return MeasurementConversionScopeImpl(converter).block()
}

fun withConverter(builder: MeasurementConverter.Builder.() -> Unit, block: MeasurementConversionScope.() -> Measurement): Measurement {
  return withConverter(converter(builder), block)
}

infix fun Measurement.convertTo(to: MeasurementUnit): Measurement {
  require(unit hasSameDimensionAs to) {
    "This overload is only valid if the units have the same dimension, try using the overload that takes a measurement"
  }

  if (to.isBaseUnit()) return convertToBaseUnit()

  return withConverter(
    converter { unit to to }
  ) {
    this@convertTo convertTo to
  }
}

fun Measurement.convertToBaseUnit(): Measurement {
  if (unit.isBaseUnit()) return this

  val converter = requireNotNull(MeasurementConverter.baseConverters.find { it.from == unit }) {
    "No base unit converter found for ${unit.name}"
  }

  return withConverter(converter) {
    this@convertToBaseUnit convertTo unit.baseUnit
  }
}

infix fun Measurement.convertTo(to: Measurement): Measurement {
  val converter = if (unit hasSameDimensionAs to.unit) {
    converter { unit to to.unit }
  } else {
    converter { this@convertTo to to }
  }

  return withConverter(converter) {
    this@convertTo convertTo to.unit
  }
}

infix fun Measurement.convertBy(block: MeasurementConverter.Builder.() -> Unit): Measurement {
  val converter = converter(block)

  return withConverter(converter) {
    this@convertBy convertTo converter.ratio.right.unit
  }
}

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

@MeasurementUnitConverterDsl
interface MeasurementConversionScope {
  infix fun Measurement.convertTo(to: MeasurementUnit): Measurement
}

private class MeasurementConversionScopeImpl(val converter: MeasurementConverter) : MeasurementConversionScope {
  override fun Measurement.convertTo(to: MeasurementUnit): Measurement {
    require(unit hasSameDimensionAs converter.ratio.left.unit) {
      """
        First unit in converter block must measure the same dimension as the measurement to be converted, if necessary chain calls
        Measurement to be converted: $this
        First measurement: ${converter.ratio.left}
      """.trimIndent()
    }

    require(to hasSameDimensionAs converter.ratio.right.unit) {
      """
        Second unit in converter block must measure the same dimension unit to be converted to, if necessary chain calls
        Unit to be converted to: $this
        Second measurement: ${converter.ratio.right}
      """.trimIndent()
    }

    val left = converter.ratio.left.unit
    val right = converter.ratio.right.unit

    return if (unit == left && to == right) {
      this convertWith converter
    } else {
      this convertTo left convertWith converter convertTo to
    }
  }

  private infix fun Measurement.convertWith(converter: MeasurementConverter) = converter.convert(quantity)
  private infix fun Measurement.reverseWith(converter: MeasurementConverter) = converter.reverse(quantity)
}

@DslMarker
annotation class MeasurementUnitConverterDsl