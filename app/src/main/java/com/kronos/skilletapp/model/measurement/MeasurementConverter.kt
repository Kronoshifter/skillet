package com.kronos.skilletapp.model.measurement

class MeasurementConverter(
  val ratio: MeasurementRatio
) {
  fun convert(quantity: Float): Measurement = Measurement(quantity * ratio.decimal, ratio.right.unit)

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
}

fun converter(builder: MeasurementConverter.Builder.() -> Unit): MeasurementConverter {
  return MeasurementConverter.Builder().apply(builder).build()
}

infix fun Measurement.convertTo(to: Measurement): Measurement {
  val converter = if (unit hasSameDimensionAs to.unit) {
    converter { unit to to.unit }
  } else {
    converter { this@convertTo to to }
  }

  return this convertWith converter
}

infix fun Measurement.convertBy(block: MeasurementConverter.Builder.() -> Unit): Measurement {
  val converter = converter(block)
  require(unit hasSameDimensionAs converter.ratio.left.unit) {
    """
      First measurement in convertBy block must measure the same dimension as the measurement to be converted, if necessary chain calls
      Measurement to be converted: $this
      First measurement: ${converter.ratio.left}
    """.trimIndent()
  }

  return this convertTo converter.ratio.left convertWith converter
}

private infix fun Measurement.convertWith(converter: MeasurementConverter) = converter.convert(quantity)

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

  fun inverse(): MeasurementRatio

  fun checkMeasurementsAreSet() = check(left != Measurement.None && right != Measurement.None) { "Measurements must be initialized before getting the ratio" }

  data class Unit(
    override val left: Measurement = Measurement.None,
    override val right: Measurement = Measurement.None
  ) : MeasurementRatio {
    override val decimal: Float
      get() {
        checkMeasurementsAreSet()
        return left.unit.factor / right.unit.factor
      }

    override fun inverse() = copy(left = right, right = left)
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

    override fun inverse() = copy(left = right, right = left)
  }

  object None : MeasurementRatio {
    override val decimal: Float
      get() = 0f
    override val left: Measurement
      get() = Measurement.None
    override val right: Measurement
      get() = Measurement.None

    override fun inverse() = None
  }
}

@DslMarker
annotation class MeasurementUnitConverterDsl