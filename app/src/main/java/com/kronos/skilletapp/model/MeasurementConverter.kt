package com.kronos.skilletapp.model

abstract class MeasurementConverter(
//  protected val from: MeasurementUnit,
//  protected val to: MeasurementUnit
) {
  protected abstract val ratio: Float
  fun convert(quantity: Float): Float = quantity * ratio

  @MeasurementUnitConverterDsl
  class Builder {
    private var _ratio: MeasurementRatio = MeasurementRatio()

    fun build(): MeasurementConverter = object : MeasurementConverter() {
      override val ratio: Float
        get() = _ratio.ratio
    }

    fun ratio(block: MeasurementRatio.() -> Unit) {
      val ratio = MeasurementRatio()
      ratio.block()
      _ratio = ratio
    }

    infix fun Measurement.to(other: Measurement) = ratio { this@to to other }
    infix fun MeasurementUnit.to(other: MeasurementUnit) = ratio { this@to to other }
  }
}

fun converter(builder: MeasurementConverter.Builder.() -> Unit): MeasurementConverter {
  return MeasurementConverter.Builder().apply(builder).build()
}

fun volumeConverter(from: MeasurementUnit.Volume, to: MeasurementUnit.Volume) = converter { from to to }
fun massConverter(from: MeasurementUnit.Mass, to: MeasurementUnit.Mass) = converter { from to to }

@DslMarker
annotation class MeasurementUnitConverterDsl

@MeasurementUnitConverterDsl
class MeasurementRatio {
  internal constructor()

  var left: Measurement = Measurement.None
    private set
  var right: Measurement = Measurement.None
    private set
  var isUnitRatio: Boolean = false
    private set

  val quantityRatio: Float
    get() {
      checkMeasurementsAreSet()
      return right.quantity / left.quantity
    }

  val unitRatio: Float
    get() {
      checkMeasurementsAreSet()
      return left.unit.factor / right.unit.factor
    }

  val ratio: Float
    get() {
      checkMeasurementsAreSet()
      return if (isUnitRatio) unitRatio else quantityRatio
    }

  private fun checkMeasurementsAreSet() = check(left != Measurement.None && right != Measurement.None) { "Measurements must be initialized before getting the ratio" }

  fun inverse() = MeasurementRatio().also {
    it.left = right
    it.right = left
  }

  infix fun Measurement.to(other: Measurement) {
    left = this
    right = other
    isUnitRatio = false
  }

  infix fun MeasurementUnit.to(other: MeasurementUnit) {
    (1 of this) to (1 of other)
    isUnitRatio = true
  }
}
