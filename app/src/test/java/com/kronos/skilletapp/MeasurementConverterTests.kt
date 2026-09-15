package com.kronos.skilletapp

import com.kronos.measurement.model.Measurement
import com.kronos.measurement.model.MeasurementUnit
import com.kronos.skilletapp.database.MeasurementConverters
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MeasurementConverterTests :
  FunSpec({
    context("MeasurementConverters Tests") {
      val converters = MeasurementConverters()

      context("Named unit round-trip") {
        test("Gram measurement preserves quantity and unit") {
          val original = Measurement(2f, MeasurementUnit.Gram)

          val restored = converters.measurementFromString(converters.measurementToJson(original))

          restored.quantity shouldBe 2f
          restored.unit shouldBe MeasurementUnit.Gram
        }

        test("Cup measurement preserves quantity and unit") {
          val original = Measurement(1.5f, MeasurementUnit.Cup)

          val restored = converters.measurementFromString(converters.measurementToJson(original))

          restored.quantity shouldBe 1.5f
          restored.unit shouldBe MeasurementUnit.Cup
        }
      }

      context("Custom unit round-trip") {
        test("Custom measurement preserves quantity and name") {
          val original = Measurement(1.5f, MeasurementUnit.Custom("sprig"))

          val restored = converters.measurementFromString(converters.measurementToJson(original))

          restored.quantity shouldBe 1.5f
          restored.unit shouldBe MeasurementUnit.Custom("sprig")
          (restored.unit as MeasurementUnit.Custom).name shouldBe "sprig"
        }
      }
    }
  })
