package com.kronos.skilletapp.model.measurement

interface MeasurementConverter {
  val ratio: Float
  fun convert(quantity: Float): Float = quantity * ratio

  @MeasurementUnitConverterDsl
  class Builder {
    private var _ratio: MeasurementRatio = MeasurementRatio.None

    fun build(): MeasurementConverter = object : MeasurementConverter {
      override val ratio: Float
        get() {
          check(_ratio != MeasurementRatio.None) { "Ratio must be initialized before getting the ratio, use the 'ratio' function, or use the extension functions for Measurement or MeasurementUnit" }
          return _ratio.ratio
        }
    }

    fun ratio(init: RatioBuilder.() -> MeasurementRatio) {
      _ratio = RatioBuilder().init()
    }

    infix fun Measurement.to(other: Measurement) = ratio {
      quantity { this@to to other }
    }

    infix fun MeasurementUnit.to(other: MeasurementUnit) = ratio {
      unit { this@to to other }
    }
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
class RatioBuilder {
  fun unit(init: MeasurementRatio.Unit.() -> Unit): MeasurementRatio {
    return MeasurementRatio.Unit().apply(init)
  }

  fun quantity(init: MeasurementRatio.Quantity.() -> Unit): MeasurementRatio {
    return MeasurementRatio.Quantity().apply(init)
  }
}

@MeasurementUnitConverterDsl
sealed class MeasurementRatio {
  abstract val ratio: Float

  var left: Measurement = Measurement.None
    protected set
  var right: Measurement = Measurement.None
    protected set

  abstract fun inverse(): MeasurementRatio

  protected fun checkMeasurementsAreSet() = check(left != Measurement.None && right != Measurement.None) { "Measurements must be initialized before getting the ratio" }

  class Unit : MeasurementRatio {
    internal constructor()

    override val ratio: Float
      get() {
        checkMeasurementsAreSet()
        return left.unit.factor / right.unit.factor
      }

    override fun inverse() = Unit().also {
      it.left = right
      it.right = left
    }

    infix fun MeasurementUnit.to(other: MeasurementUnit) {
      left = 1 of this
      right = 1 of other
    }
  }

  class Quantity : MeasurementRatio {
    internal constructor()

    override val ratio: Float
      get() {
        checkMeasurementsAreSet()
        return right.quantity / left.quantity
      }

    override fun inverse() = Quantity().also {
      it.left = right
      it.right = left
    }

    infix fun Measurement.to(other: Measurement) {
      left = this
      right = other
    }
  }

  object None : MeasurementRatio() {
    override val ratio: Float
      get() = 0f

    override fun inverse() = None
  }
}
