package com.kronos.skilletapp

import com.kronos.skilletapp.model.measurement.Measurement
import com.kronos.skilletapp.model.measurement.MeasurementUnit
import com.kronos.skilletapp.model.measurement.convertBy
import com.kronos.skilletapp.model.measurement.convertTo
import com.kronos.skilletapp.model.measurement.of
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MeasurementTests : FunSpec({
  val grams = Measurement(10f, MeasurementUnit.Gram)
  val kilogram = Measurement(1f, MeasurementUnit.Kilogram)
  val ounces = Measurement(16f, MeasurementUnit.Ounce)
  val pound = Measurement(1f, MeasurementUnit.Pound)

  val milliliters = Measurement(100f, MeasurementUnit.Milliliter)
  val liter = Measurement(1f, MeasurementUnit.Liter)
  val teaspoon = Measurement(1f, MeasurementUnit.Teaspoon)
  val tablespoon = Measurement(1f, MeasurementUnit.Tablespoon)
  val fluidOunce = Measurement(1f, MeasurementUnit.FluidOunce)
  val cup = Measurement(1f, MeasurementUnit.Cup)
  val pint = Measurement(1f, MeasurementUnit.Pint)
  val quart = Measurement(1f, MeasurementUnit.Quart)
  val gallon = Measurement(1f, MeasurementUnit.Gallon)

  val json = Json {
    ignoreUnknownKeys = true
  }

  context("Scaling") {
    test("Double") {
      val scaled = grams.scale(2f)
      scaled.quantity shouldBe (20f plusOrMinus 0.001f)
    }

    test("Triple") {
      val scaled = grams.scale(3f)
      scaled.quantity shouldBe (30f plusOrMinus 0.001f)
    }

    test("Zero") {
      val scaled = grams.scale(0f)
      scaled.quantity shouldBe (0f plusOrMinus 0.001f)
    }

    test("Identity") {
      val scaled = grams.scale(1f)
      scaled.quantity shouldBe (10f plusOrMinus 0.001f)
    }

    test("Negative") {
      val scaled = grams.scale(-1f)
      scaled.quantity shouldBe (-10f plusOrMinus 0.001f)
    }

    test("Half") {
      val scaled = grams.scale(0.5f)
      scaled.quantity shouldBe (5f plusOrMinus 0.001f)
    }

    test("Quarter") {
      val scaled = grams.scale(0.25f)
      scaled.quantity shouldBe (2.5f plusOrMinus 0.001f)
    }
  }

  context("Conversions") {
    context("Mass") {
      context("Metric") {
        test("Grams to Kilograms") {
          val converted = grams convertTo MeasurementUnit.Kilogram
          converted.quantity shouldBe (0.01f plusOrMinus 0.001f)
          converted.unit shouldBe MeasurementUnit.Kilogram
        }

        test("Kilograms to Grams") {
          val converted = kilogram convertTo MeasurementUnit.Gram
          converted.quantity shouldBe (1000f plusOrMinus 0.001f)
          converted.unit shouldBe MeasurementUnit.Gram
        }
      }

      context("Imperial") {
        test("Ounces to Pounds") {
          val converted = ounces convertTo MeasurementUnit.Pound
          converted.quantity shouldBe (1f plusOrMinus 0.001f)
          converted.unit shouldBe MeasurementUnit.Pound
        }

        test("Pounds to Ounces") {
          val converted = pound convertTo MeasurementUnit.Ounce
          converted.quantity shouldBe (16f plusOrMinus 0.001f)
          converted.unit shouldBe MeasurementUnit.Ounce
        }
      }

      context("Metric to Imperial") {
        test("Grams to Ounces") {
          val converted = grams convertTo MeasurementUnit.Ounce
          converted.quantity shouldBe (0.35274f plusOrMinus 0.001f)
          converted.unit shouldBe MeasurementUnit.Ounce
        }

        test("Kilograms to Pounds") {
          val converted = kilogram convertTo MeasurementUnit.Pound
          converted.quantity shouldBe (2.20462f plusOrMinus 0.001f)
          converted.unit shouldBe MeasurementUnit.Pound
        }
      }

      context("Imperial to Metric") {
        test("Ounces to Grams") {
          val converted = ounces convertTo MeasurementUnit.Gram
          converted.quantity shouldBe (453.592f plusOrMinus 0.001f)
          converted.unit shouldBe MeasurementUnit.Gram
        }

        test("Pounds to Kilograms") {
          val converted = pound convertTo MeasurementUnit.Kilogram
          converted.quantity shouldBe (0.453592f plusOrMinus 0.001f)
          converted.unit shouldBe MeasurementUnit.Kilogram
        }
      }
    }

    context("Volume") {
      context("Metric") {
        test("Milliliter to Liter") {
          val converted = milliliters convertTo MeasurementUnit.Liter
          converted.quantity shouldBe (0.1f plusOrMinus 0.001f)
          converted.unit shouldBe MeasurementUnit.Liter
        }

        test("Liter to Milliliter") {
          val converted = liter convertTo MeasurementUnit.Milliliter
          converted.quantity shouldBe (1000f plusOrMinus 0.001f)
          converted.unit shouldBe MeasurementUnit.Milliliter
        }
      }

      context("Imperial") {
        test("Gallons to Quarts") {
          val converted = gallon convertTo MeasurementUnit.Quart
          converted.quantity shouldBe (4f plusOrMinus 0.001f)
          converted.unit shouldBe MeasurementUnit.Quart
        }

        test("Quarts to Gallons") {
          val converted = quart convertTo MeasurementUnit.Gallon
          converted.quantity shouldBe (0.25f plusOrMinus 0.001f)
          converted.unit shouldBe MeasurementUnit.Gallon
        }

        test("Quarts to Pints") {
          val converted = quart convertTo MeasurementUnit.Pint
          converted.quantity shouldBe (2f plusOrMinus 0.001f)
          converted.unit shouldBe MeasurementUnit.Pint
        }

        test("Pints to Quarts") {
          val converted = pint convertTo MeasurementUnit.Quart
          converted.quantity shouldBe (0.5f plusOrMinus 0.001f)
          converted.unit shouldBe MeasurementUnit.Quart
        }

        test("Cups to Tablespoons") {
          val converted = cup convertTo MeasurementUnit.Tablespoon
          converted.quantity shouldBe (16f plusOrMinus 0.001f)
          converted.unit shouldBe MeasurementUnit.Tablespoon
        }

        test("Tablespoons to Cups") {
          val converted = tablespoon convertTo MeasurementUnit.Cup
          converted.quantity shouldBe (0.0625f plusOrMinus 0.001f)
          converted.unit shouldBe MeasurementUnit.Cup
        }

        test("Cups to Fluid Ounces") {
          val converted = cup convertTo MeasurementUnit.FluidOunce
          converted.quantity shouldBe (8f plusOrMinus 0.001f)
          converted.unit shouldBe MeasurementUnit.FluidOunce
        }

        test("Fluid Ounces to Cups") {
          val converted = fluidOunce convertTo MeasurementUnit.Cup
          converted.quantity shouldBe (0.125f plusOrMinus 0.001f)
          converted.unit shouldBe MeasurementUnit.Cup
        }

        test("Fluid Ounces to Tablespoons") {
          val converted = fluidOunce convertTo MeasurementUnit.Tablespoon
          converted.quantity shouldBe (2f plusOrMinus 0.001f)
          converted.unit shouldBe MeasurementUnit.Tablespoon
        }

        test("Tablespoons to Fluid Ounces") {
          val converted = tablespoon convertTo MeasurementUnit.FluidOunce
          converted.quantity shouldBe (0.5f plusOrMinus 0.001f)
          converted.unit shouldBe MeasurementUnit.FluidOunce
        }

        test("Tablespoons to Teaspoons") {
          val converted = tablespoon convertTo MeasurementUnit.Teaspoon
          converted.quantity shouldBe (3f plusOrMinus 0.001f)
          converted.unit shouldBe MeasurementUnit.Teaspoon
        }

        test("Teaspoons to Tablespoons") {
          val converted = teaspoon convertTo MeasurementUnit.Tablespoon
          converted.quantity shouldBe (0.333f plusOrMinus 0.001f)
          converted.unit shouldBe MeasurementUnit.Tablespoon
        }
      }

      context("Metric to Imperial") {
        test("Milliliters to Fluid Ounces") {
          val converted = milliliters convertTo MeasurementUnit.FluidOunce
          converted.quantity shouldBe (3.381f plusOrMinus 0.001f)
          converted.unit shouldBe MeasurementUnit.FluidOunce
        }

        test("Milliliters to Cups") {
          val converted = milliliters convertTo MeasurementUnit.Cup
          converted.quantity shouldBe (0.4232f plusOrMinus 0.001f)
          converted.unit shouldBe MeasurementUnit.Cup
        }
      }

      context("Imperial to Metric") {
        test("Tablespoons to Milliliters") {
          val converted = tablespoon convertTo MeasurementUnit.Milliliter
          converted.quantity shouldBe (14.7868f plusOrMinus 0.001f)
          converted.unit shouldBe MeasurementUnit.Milliliter
        }

        test("Cups to Milliliters") {
          val converted = cup convertTo MeasurementUnit.Milliliter
          converted.quantity shouldBe (236.588f plusOrMinus 0.001f)
          converted.unit shouldBe MeasurementUnit.Milliliter
        }
      }
    }

    context("Cross-Dimension Conversions") {
      test("Volume to Mass") {
        val tablespoonsButter = Measurement(2f, MeasurementUnit.Tablespoon)
        val gramsButter = tablespoonsButter.convertBy {
          (1 of MeasurementUnit.Tablespoon) to (14 of MeasurementUnit.Gram)
        }

        gramsButter.quantity shouldBe (28f plusOrMinus 0.001f)
        gramsButter.unit shouldBe MeasurementUnit.Gram
      }

      test("Mass to Volume") {
        val gramsButter = Measurement(28f, MeasurementUnit.Gram)
        val tablespoonsButter = gramsButter.convertBy {
          (14 of MeasurementUnit.Gram) to (1 of MeasurementUnit.Tablespoon)
        }

        tablespoonsButter.quantity shouldBe (2f plusOrMinus 0.001f)
        tablespoonsButter.unit shouldBe MeasurementUnit.Tablespoon
      }

      test("Custom to Mass") {
        val garlic = Measurement(1f, MeasurementUnit.Custom("cloves"))
        val gramsGarlic = garlic.convertBy {
          garlic to (3 of MeasurementUnit.Gram)
        }

        gramsGarlic.quantity shouldBe (3f plusOrMinus 0.001f)
        gramsGarlic.unit shouldBe MeasurementUnit.Gram
      }

      test("Custom to Volume") {
        val garlic = Measurement(2f, MeasurementUnit.Custom("cloves"))
        val gramsGarlic = garlic.convertBy {
          (1 of MeasurementUnit.Custom("cloves")) to (0.5f of MeasurementUnit.Teaspoon)
        }

        gramsGarlic.quantity shouldBe (1f plusOrMinus 0.001f)
        gramsGarlic.unit shouldBe MeasurementUnit.Teaspoon
      }

      test("None to mass") {
        val coconut = Measurement(1f, MeasurementUnit.None)
        val gramsCoconut = coconut.convertBy {
          coconut to (400 of MeasurementUnit.Gram)
        }

        gramsCoconut.quantity shouldBe (400f plusOrMinus 0.001f)
        gramsCoconut.unit shouldBe MeasurementUnit.Gram
      }

      test("None to volume") {
        val coconut = Measurement(2f, MeasurementUnit.None)
        val quarterCupCoconut = coconut.convertBy {
          (1 of MeasurementUnit.None) to (0.25f of MeasurementUnit.Cup)
        }

        quarterCupCoconut.quantity shouldBe (0.5f plusOrMinus 0.001f)
        quarterCupCoconut.unit shouldBe MeasurementUnit.Cup
      }
    }

    context("Improper Conversions") {
      test("Improper Volume to Mass") {
        shouldThrowAny {
          teaspoon convertTo MeasurementUnit.Gram
        }
      }

      test("Improper Mass to Volume") {
        shouldThrowAny {
          grams convertTo MeasurementUnit.Teaspoon
        }
      }
    }

    context("Misc") {
      test("Normalization of no unit") {
        val measurement = Measurement(0f, MeasurementUnit.None)
        val normalized = measurement.normalized()
        normalized.quantity shouldBe 0f
        normalized.unit shouldBe MeasurementUnit.None
      }
    }
  }

  context("Normalizing") {

    context("Teaspoon") {
      test("Teaspoon to Tablespoon") {
        val normalized = Measurement(3f, MeasurementUnit.Teaspoon).normalized()
        normalized.quantity shouldBe (1f plusOrMinus 0.001f)
        normalized.unit shouldBe MeasurementUnit.Tablespoon
      }

      test("Teaspoon to Cup") {
        val normalized = Measurement(48f, MeasurementUnit.Teaspoon).normalized()
        normalized.quantity shouldBe (1f plusOrMinus 0.001f)
        normalized.unit shouldBe MeasurementUnit.Cup
      }
    }
  }

  context("Scaling and Normalizing") {
    context("Metric") {

    }

    context("Imperial") {
      test("Teaspoon to Tablespoon") {
        val scaled = teaspoon.scaleAndNormalize(3f).roundToEighth()
        scaled.quantity shouldBe (1f plusOrMinus 0.001f)
        scaled.unit shouldBe MeasurementUnit.Tablespoon
      }

      test("Tablespoon to Teaspoon") {
        val scaled = tablespoon.scaleAndNormalize(0.333f).roundToEighth()
        scaled.quantity shouldBe (1f plusOrMinus 0.001f)
        scaled.unit shouldBe MeasurementUnit.Teaspoon
      }

      test("Tablespoon to Cup") {
        val scaled = tablespoon.scaleAndNormalize(24f).roundToEighth()
        scaled.quantity shouldBe (1.5f plusOrMinus 0.001f)
        scaled.unit shouldBe MeasurementUnit.Cup
      }
    }
  }

  context("Scale and Round") {
    test("0.90834298 * 23") {
      val m = Measurement(0.90834298f, MeasurementUnit.Milliliter)
      val scaled = m.scaleAndRound(23f)
      scaled.quantity shouldBe (20.875f plusOrMinus 0.001f)
      scaled.unit shouldBe MeasurementUnit.Milliliter
    }

    test("10.0624f * 1.5") {
      val m = Measurement(10.0624f, MeasurementUnit.Milliliter)
      val scaled = m.scaleAndRound(1.5f)
      scaled.quantity shouldBe (15.125f plusOrMinus 0.001f)
      scaled.unit shouldBe MeasurementUnit.Milliliter
    }

    test(".125 / 2") {
      val m = Measurement(0.125f, MeasurementUnit.Cup)
      val scaled = m.scaleAndRound(0.5f)
      scaled.quantity shouldBe (0.125f plusOrMinus 0.001f)
      scaled.unit shouldBe MeasurementUnit.Cup
    }
  }

  context("Comparison") {
    context("Same Unit") {
      context("Equality") {
        test("Teaspoon") {
          val m1 = Measurement(1f, MeasurementUnit.Teaspoon)
          val m2 = Measurement(1f, MeasurementUnit.Teaspoon)
          (m1 == m2) shouldBe true
        }

        test("Tablespoon") {
          val m1 = Measurement(1f, MeasurementUnit.Tablespoon)
          val m2 = Measurement(1f, MeasurementUnit.Tablespoon)
          (m1 == m2) shouldBe true
        }

        test("Cup") {
          val m1 = Measurement(1f, MeasurementUnit.Cup)
          val m2 = Measurement(1f, MeasurementUnit.Cup)
          (m1 == m2) shouldBe true
        }
      }

      context("Inequality") {
        test("Teaspoon") {
          val m1 = Measurement(1f, MeasurementUnit.Teaspoon)
          val m2 = Measurement(2f, MeasurementUnit.Teaspoon)
          (m1 != m2) shouldBe true
        }

        test("Tablespoon") {
          val m1 = Measurement(1f, MeasurementUnit.Tablespoon)
          val m2 = Measurement(2f, MeasurementUnit.Tablespoon)
          (m1 != m2) shouldBe true
        }

        test("Cup") {
          val m1 = Measurement(1f, MeasurementUnit.Cup)
          val m2 = Measurement(2f, MeasurementUnit.Cup)
          (m1 != m2) shouldBe true
        }
      }

      context("Greater Than") {
        test("Greater") {
          val m1 = Measurement(2f, MeasurementUnit.Teaspoon)
          val m2 = Measurement(1f, MeasurementUnit.Teaspoon)
          (m1 > m2) shouldBe true
        }

        test("Not Greater") {
          val m1 = Measurement(1f, MeasurementUnit.Teaspoon)
          val m2 = Measurement(1f, MeasurementUnit.Teaspoon)
          (m1 > m2) shouldBe false
        }

        test("Or Equal To") {
          val m1 = Measurement(1f, MeasurementUnit.Teaspoon)
          val m2 = Measurement(1f, MeasurementUnit.Teaspoon)
          (m1 >= m2) shouldBe true
        }
      }

      context("Less Than") {
        test("Less") {
          val m1 = Measurement(1f, MeasurementUnit.Teaspoon)
          val m2 = Measurement(2f, MeasurementUnit.Teaspoon)
          (m1 < m2) shouldBe true
        }

        test("Not Less") {
          val m1 = Measurement(1f, MeasurementUnit.Teaspoon)
          val m2 = Measurement(1f, MeasurementUnit.Teaspoon)
          (m1 < m2) shouldBe false
        }

        test("Or Equal To") {
          val m1 = Measurement(1f, MeasurementUnit.Teaspoon)
          val m2 = Measurement(1f, MeasurementUnit.Teaspoon)
          (m1 <= m2) shouldBe true
        }
      }
    }

    context("Different Units") {
      context("Equality") {
        test("Teaspoon & Tablespoon") {
          val m1 = Measurement(3f, MeasurementUnit.Teaspoon)
          val m2 = Measurement(1f, MeasurementUnit.Tablespoon)
          (m1 == m2) shouldBe true
        }

        test("Cup & Fluid Ounces") {
          val m1 = Measurement(1f, MeasurementUnit.Cup)
          val m2 = Measurement(8f, MeasurementUnit.FluidOunce)
          (m1 == m2) shouldBe true
        }

        test("Fluid Ounces & Tablespoons") {
          val m1 = Measurement(1f, MeasurementUnit.FluidOunce)
          val m2 = Measurement(2f, MeasurementUnit.Tablespoon)
          (m1 == m2) shouldBe true
        }
      }

      context("Inequality") {
        test("Teaspoon & Tablespoon") {
          val m1 = Measurement(1f, MeasurementUnit.Teaspoon)
          val m2 = Measurement(1f, MeasurementUnit.Tablespoon)
          (m1 != m2) shouldBe true
        }

        test("Cup & Fluid Ounces") {
          val m1 = Measurement(1f, MeasurementUnit.Cup)
          val m2 = Measurement(1f, MeasurementUnit.FluidOunce)
          (m1 != m2) shouldBe true
        }

        test("Fluid Ounces & Tablespoons") {
          val m1 = Measurement(1f, MeasurementUnit.FluidOunce)
          val m2 = Measurement(1f, MeasurementUnit.Tablespoon)
          (m1 != m2) shouldBe true
        }
      }

      context("Greater Than") {
        test("Greater") {
          val m1 = Measurement(1f, MeasurementUnit.Tablespoon)
          val m2 = Measurement(1f, MeasurementUnit.Teaspoon)
          (m1 > m2) shouldBe true
        }

        test("Not Greater") {
          val m1 = Measurement(1f, MeasurementUnit.Teaspoon)
          val m2 = Measurement(1f, MeasurementUnit.Tablespoon)
          (m1 > m2) shouldBe false
        }

        test("Or Equal To") {
          val m1 = Measurement(3f, MeasurementUnit.Teaspoon)
          val m2 = Measurement(1f, MeasurementUnit.Tablespoon)
          (m1 >= m2) shouldBe true
        }
      }

      context("Less Than") {
        test("Less") {
          val m1 = Measurement(1f, MeasurementUnit.Tablespoon)
          val m2 = Measurement(1f, MeasurementUnit.Teaspoon)
          (m2 < m1) shouldBe true
        }

        test("Not") {
          val m1 = Measurement(1f, MeasurementUnit.Teaspoon)
          val m2 = Measurement(1f, MeasurementUnit.Tablespoon)
          (m2 < m1) shouldBe false
        }

        test("Or Equal To") {
          val m1 = Measurement(3f, MeasurementUnit.Teaspoon)
          val m2 = Measurement(1f, MeasurementUnit.Tablespoon)
          (m2 <= m1) shouldBe true
        }
      }
    }
  }

  context("Arithmetic") {
    context("Addition") {
      test("Teaspoon + Tablespoon") {
        val m1 = Measurement(1f, MeasurementUnit.Teaspoon)
        val m2 = Measurement(1f, MeasurementUnit.Tablespoon)
        val sum = m1 + m2
        sum.quantity shouldBe (4f plusOrMinus 0.001f)
        sum.unit shouldBe MeasurementUnit.Teaspoon
      }

      test("Tablespoon + Cup") {
        val m1 = Measurement(8f, MeasurementUnit.Tablespoon)
        val m2 = Measurement(1f, MeasurementUnit.Cup)
        val sum = m1 + m2
        sum.quantity shouldBe (24f plusOrMinus 0.001f)
        sum.unit shouldBe MeasurementUnit.Tablespoon
      }

      test("Cup + Fluid Ounces") {
        val m1 = Measurement(1f, MeasurementUnit.Cup)
        val m2 = Measurement(8f, MeasurementUnit.FluidOunce)
        val sum = m1 + m2
        sum.quantity shouldBe (2f plusOrMinus 0.001f)
        sum.unit shouldBe MeasurementUnit.Cup
      }

      context("Unsafe Addition") {
        test("Cup + Kilogram") {
          val m1 = Measurement(1f, MeasurementUnit.Cup)
          val m2 = Measurement(8f, MeasurementUnit.Kilogram)

          shouldThrowAny {
            m1 + m2
          }
        }
      }
    }

    context("Subtraction") {
      test("Teaspoon - Tablespoon") {
        val m1 = Measurement(4f, MeasurementUnit.Teaspoon)
        val m2 = Measurement(1f, MeasurementUnit.Tablespoon)
        val diff = m1 - m2
        diff.quantity shouldBe (1f plusOrMinus 0.001f)
        diff.unit shouldBe MeasurementUnit.Teaspoon
      }

      test("Cup - Tablespoon") {
        val m1 = Measurement(2f, MeasurementUnit.Cup)
        val m2 = Measurement(8f, MeasurementUnit.Tablespoon)
        val diff = m1 - m2
        diff.quantity shouldBe (1.5f plusOrMinus 0.001f)
        diff.unit shouldBe MeasurementUnit.Cup
      }

      test("Cup - Fluid Ounces") {
        val m1 = Measurement(2f, MeasurementUnit.Cup)
        val m2 = Measurement(8f, MeasurementUnit.FluidOunce)
        val diff = m1 - m2
        diff.quantity shouldBe (1f plusOrMinus 0.001f)
        diff.unit shouldBe MeasurementUnit.Cup
      }

      context("Unsafe Subtraction") {
        test("Cup - Kilogram") {
          val m1 = Measurement(2f, MeasurementUnit.Cup)
          val m2 = Measurement(8f, MeasurementUnit.Kilogram)
          shouldThrowAny {
            m1 - m2
          }
        }
      }
    }
  }
})