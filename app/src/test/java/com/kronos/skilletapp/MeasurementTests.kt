package com.kronos.skilletapp

import com.kronos.skilletapp.model.Measurement
import com.kronos.skilletapp.model.MeasurementUnit
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe

class MeasurementTests : FunSpec({
  val grams = Measurement(10.0, MeasurementUnit.Gram)
  val kilogram = Measurement(1.0, MeasurementUnit.Kilogram)
  val ounces = Measurement(16.0, MeasurementUnit.Ounce)
  val pound = Measurement(1.0, MeasurementUnit.Pound)

  val milliliters = Measurement(100.0, MeasurementUnit.Milliliter)
  val liter = Measurement(1.0, MeasurementUnit.Liter)
  val teaspoon = Measurement(1.0, MeasurementUnit.Teaspoon)
  val tablespoon = Measurement(1.0, MeasurementUnit.Tablespoon)
  val fluidOunce = Measurement(1.0, MeasurementUnit.FluidOunce)
  val cup = Measurement(1.0, MeasurementUnit.Cup)
  val pint = Measurement(1.0, MeasurementUnit.Pint)
  val quart = Measurement(1.0, MeasurementUnit.Quart)
  val gallon = Measurement(1.0, MeasurementUnit.Gallon)

  context("Scaling") {
    test("Double") {
      val scaled = grams.scale(2.0)
      scaled.quantity shouldBe (20.0 plusOrMinus 0.001)
    }

    test("Triple") {
      val scaled = grams.scale(3.0)
      scaled.quantity shouldBe (30.0 plusOrMinus 0.001)
    }

    test("Zero") {
      val scaled = grams.scale(0.0)
      scaled.quantity shouldBe (0.0 plusOrMinus 0.001)
    }

    test("Identity") {
      val scaled = grams.scale(1.0)
      scaled.quantity shouldBe (10.0 plusOrMinus 0.001)
    }

    test("Negative") {
      val scaled = grams.scale(-1.0)
      scaled.quantity shouldBe (-10.0 plusOrMinus 0.001)
    }

    test("Half") {
      val scaled = grams.scale(0.5)
      scaled.quantity shouldBe (5.0 plusOrMinus 0.001)
    }

    test("Quarter") {
      val scaled = grams.scale(0.25)
      scaled.quantity shouldBe (2.5 plusOrMinus 0.001)
    }
  }

  context("Conversions") {
    context("Mass") {
      context("Metric") {
        test("Grams to Kilograms") {
          val converted = grams.convert(MeasurementUnit.Kilogram)
          converted.quantity shouldBe (0.01 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Kilogram
        }

        test("Kilograms to Grams") {
          val converted = kilogram.convert(MeasurementUnit.Gram)
          converted.quantity shouldBe (1000.0 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Gram
        }
      }

      context("Imperial") {
        test("Ounces to Pounds") {
          val converted = ounces.convert(MeasurementUnit.Pound)
          converted.quantity shouldBe (1.0 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Pound
        }

        test("Pounds to Ounces") {
          val converted = pound.convert(MeasurementUnit.Ounce)
          converted.quantity shouldBe (16.0 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Ounce
        }
      }

      context("Metric to Imperial") {
        test("Grams to Ounces") {
          val converted = grams.convert(MeasurementUnit.Ounce)
          converted.quantity shouldBe (0.35274 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Ounce
        }

        test("Kilograms to Pounds") {
          val converted = kilogram.convert(MeasurementUnit.Pound)
          converted.quantity shouldBe (2.20462 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Pound
        }
      }

      context("Imperial to Metric") {
        test("Ounces to Grams") {
          val converted = ounces.convert(MeasurementUnit.Gram)
          converted.quantity shouldBe (453.592 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Gram
        }

        test("Pounds to Kilograms") {
          val converted = pound.convert(MeasurementUnit.Kilogram)
          converted.quantity shouldBe (0.453592 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Kilogram
        }
      }
    }

    context("Volume") {
      context("Metric") {
        test("Milliliter to Liter") {
          val converted = milliliters.convert(MeasurementUnit.Liter)
          converted.quantity shouldBe (0.1 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Liter
        }

        test("Liter to Milliliter") {
          val converted = liter.convert(MeasurementUnit.Milliliter)
          converted.quantity shouldBe (1000.0 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Milliliter
        }
      }

      context("Imperial") {
        test("Gallons to Quarts") {
          val converted = gallon.convert(MeasurementUnit.Quart)
          converted.quantity shouldBe (4.0 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Quart
        }

        test("Quarts to Gallons") {
          val converted = quart.convert(MeasurementUnit.Gallon)
          converted.quantity shouldBe (0.25 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Gallon
        }

        test("Quarts to Pints") {
          val converted = quart.convert(MeasurementUnit.Pint)
          converted.quantity shouldBe (2.0 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Pint
        }

        test("Pints to Quarts") {
          val converted = pint.convert(MeasurementUnit.Quart)
          converted.quantity shouldBe (0.5 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Quart
        }

        test("Cups to Tablespoons") {
          val converted = cup.convert(MeasurementUnit.Tablespoon)
          converted.quantity shouldBe (16.0 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Tablespoon
        }

        test("Tablespoons to Cups") {
          val converted = tablespoon.convert(MeasurementUnit.Cup)
          converted.quantity shouldBe (0.0625 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Cup
        }

        test("Cups to Fluid Ounces") {
          val converted = cup.convert(MeasurementUnit.FluidOunce)
          converted.quantity shouldBe (8.0 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.FluidOunce
        }

        test("Fluid Ounces to Cups") {
          val converted = fluidOunce.convert(MeasurementUnit.Cup)
          converted.quantity shouldBe (0.125 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Cup
        }

        test("Fluid Ounces to Tablespoons") {
          val converted = fluidOunce.convert(MeasurementUnit.Tablespoon)
          converted.quantity shouldBe (2.0 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Tablespoon
        }

        test("Tablespoons to Fluid Ounces") {
          val converted = tablespoon.convert(MeasurementUnit.FluidOunce)
          converted.quantity shouldBe (0.5 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.FluidOunce
        }

        test("Tablespoons to Teaspoons") {
          val converted = tablespoon.convert(MeasurementUnit.Teaspoon)
          converted.quantity shouldBe (3.0 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Teaspoon
        }

        test("Teaspoons to Tablespoons") {
          val converted = teaspoon.convert(MeasurementUnit.Tablespoon)
          converted.quantity shouldBe (0.333 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Tablespoon
        }
      }

      context("Metric to Imperial") {
        test("Milliliters to Fluid Ounces") {
          val converted = milliliters.convert(MeasurementUnit.FluidOunce)
          converted.quantity shouldBe (3.381 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.FluidOunce
        }

        test("Milliliters to Cups") {
          val converted = milliliters.convert(MeasurementUnit.Cup)
          converted.quantity shouldBe (0.4232 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Cup
        }
      }

      context("Imperial to Metric") {
        test("Tablespoons to Milliliters") {
          val converted = tablespoon.convert(MeasurementUnit.Milliliter)
          converted.quantity shouldBe (14.7868 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Milliliter
        }

        test("Cups to Milliliters") {
          val converted = cup.convert(MeasurementUnit.Milliliter)
          converted.quantity shouldBe (236.588 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Milliliter
        }
      }
    }

    context("Custom Conversions") {
      test("Volume to Mass") {
        val tablespoonsButter = Measurement(2.0, MeasurementUnit.Tablespoon)
        val gramsButter = tablespoonsButter.convert(MeasurementUnit.Gram) { it * 14 }

        gramsButter.quantity shouldBe (28.0 plusOrMinus 0.001)
        gramsButter.unit shouldBe MeasurementUnit.Gram
      }

      test("Mass to Volume") {
        val gramsButter = Measurement(28.0, MeasurementUnit.Gram)
        val tablespoonsButter = gramsButter.convert(MeasurementUnit.Tablespoon) { it / 14 }

        tablespoonsButter.quantity shouldBe (2.0 plusOrMinus 0.001)
        tablespoonsButter.unit shouldBe MeasurementUnit.Tablespoon
      }
    }

    context("Improper Conversions") {
      test("Improper Volume to Mass") {
        shouldThrowAny {
          teaspoon.convert(MeasurementUnit.Gram)
        }
      }

      test("Improper Mass to Volume") {
        shouldThrowAny {
          grams.convert(MeasurementUnit.Teaspoon)
        }
      }
    }

    context("Misc") {
      test("Normalization of no unit") {
        val measurement = Measurement(0.0, MeasurementUnit.None)
        val normalized = measurement.normalize()
        normalized.quantity shouldBe 0.0
        normalized.unit shouldBe MeasurementUnit.None
      }
    }
  }

  context("Normalizing") {

    context("Teaspoon") {
      test("Teaspoon to Tablespoon") {
        val normalized = Measurement(3.0, MeasurementUnit.Teaspoon).normalize()
        normalized.quantity shouldBe (1.0 plusOrMinus 0.001)
        normalized.unit shouldBe MeasurementUnit.Tablespoon
      }

      test("Teaspoon to Cup") {
        val normalized = Measurement(48.0, MeasurementUnit.Teaspoon).normalize()
        normalized.quantity shouldBe (1.0 plusOrMinus 0.001)
        normalized.unit shouldBe MeasurementUnit.Cup
      }
    }
  }

  context("Scaling and Normalizing") {
    context("Metric") {

    }

    context("Imperial") {
      test("Teaspoon to Tablespoon") {
        val scaled = teaspoon.scaleAndNormalize(3.0).roundToEighth()
        scaled.quantity shouldBe (1.0 plusOrMinus 0.001)
        scaled.unit shouldBe MeasurementUnit.Tablespoon
      }

      test("Tablespoon to Teaspoon") {
        val scaled = tablespoon.scaleAndNormalize(0.333).roundToEighth()
        scaled.quantity shouldBe (1.0 plusOrMinus 0.001)
        scaled.unit shouldBe MeasurementUnit.Teaspoon
      }

      test("Tablespoon to Cup") {
        val scaled = tablespoon.scaleAndNormalize(24.0).roundToEighth()
        scaled.quantity shouldBe (1.5 plusOrMinus 0.001)
        scaled.unit shouldBe MeasurementUnit.Cup
      }
    }
  }

  context("Scale and Round") {
    test("0.90834298 * 23") {
      val m = Measurement(0.90834298, MeasurementUnit.Milliliter)
      val scaled = m.scaleAndRound(23.0)
      scaled.quantity shouldBe (20.875 plusOrMinus 0.001)
      scaled.unit shouldBe MeasurementUnit.Milliliter
    }

    test("10.0624 * 1.5") {
      val m = Measurement(10.0624, MeasurementUnit.Milliliter)
      val scaled = m.scaleAndRound(1.5)
      scaled.quantity shouldBe (15.125 plusOrMinus 0.001)
      scaled.unit shouldBe MeasurementUnit.Milliliter
    }

    test(".125 / 2") {
      val m = Measurement(0.125, MeasurementUnit.Cup)
      val scaled = m.scaleAndRound(0.5)
      scaled.quantity shouldBe (0.125 plusOrMinus 0.001)
      scaled.unit shouldBe MeasurementUnit.Cup
    }
  }

  context("Comparison") {
    context("Same Unit") {
      context("Equality") {
        test("Teaspoon") {
          val m1 = Measurement(1.0, MeasurementUnit.Teaspoon)
          val m2 = Measurement(1.0, MeasurementUnit.Teaspoon)
          (m1 == m2) shouldBe true
        }

        test("Tablespoon") {
          val m1 = Measurement(1.0, MeasurementUnit.Tablespoon)
          val m2 = Measurement(1.0, MeasurementUnit.Tablespoon)
          (m1 == m2) shouldBe true
        }

        test("Cup") {
          val m1 = Measurement(1.0, MeasurementUnit.Cup)
          val m2 = Measurement(1.0, MeasurementUnit.Cup)
          (m1 == m2) shouldBe true
        }
      }

      context("Inequality") {
        test("Teaspoon") {
          val m1 = Measurement(1.0, MeasurementUnit.Teaspoon)
          val m2 = Measurement(2.0, MeasurementUnit.Teaspoon)
          (m1 != m2) shouldBe true
        }

        test("Tablespoon") {
          val m1 = Measurement(1.0, MeasurementUnit.Tablespoon)
          val m2 = Measurement(2.0, MeasurementUnit.Tablespoon)
          (m1 != m2) shouldBe true
        }

        test("Cup") {
          val m1 = Measurement(1.0, MeasurementUnit.Cup)
          val m2 = Measurement(2.0, MeasurementUnit.Cup)
          (m1 != m2) shouldBe true
        }
      }

      context("Greater Than") {
        test("Greater") {
          val m1 = Measurement(2.0, MeasurementUnit.Teaspoon)
          val m2 = Measurement(1.0, MeasurementUnit.Teaspoon)
          (m1 > m2) shouldBe true
        }

        test("Not Greater") {
          val m1 = Measurement(1.0, MeasurementUnit.Teaspoon)
          val m2 = Measurement(1.0, MeasurementUnit.Teaspoon)
          (m1 > m2) shouldBe false
        }

        test("Or Equal To") {
          val m1 = Measurement(1.0, MeasurementUnit.Teaspoon)
          val m2 = Measurement(1.0, MeasurementUnit.Teaspoon)
          (m1 >= m2) shouldBe true
        }
      }

      context("Less Than") {
        test("Less") {
          val m1 = Measurement(1.0, MeasurementUnit.Teaspoon)
          val m2 = Measurement(2.0, MeasurementUnit.Teaspoon)
          (m1 < m2) shouldBe true
        }

        test("Not Less") {
          val m1 = Measurement(1.0, MeasurementUnit.Teaspoon)
          val m2 = Measurement(1.0, MeasurementUnit.Teaspoon)
          (m1 < m2) shouldBe false
        }

        test("Or Equal To") {
          val m1 = Measurement(1.0, MeasurementUnit.Teaspoon)
          val m2 = Measurement(1.0, MeasurementUnit.Teaspoon)
          (m1 <= m2) shouldBe true
        }
      }
    }

    context("Different Units") {
      context("Equality") {
        test("Teaspoon & Tablespoon") {
          val m1 = Measurement(3.0, MeasurementUnit.Teaspoon)
          val m2 = Measurement(1.0, MeasurementUnit.Tablespoon)
          (m1 == m2) shouldBe true
        }

        test("Cup & Fluid Ounces") {
          val m1 = Measurement(1.0, MeasurementUnit.Cup)
          val m2 = Measurement(8.0, MeasurementUnit.FluidOunce)
          (m1 == m2) shouldBe true
        }

        test("Fluid Ounces & Tablespoons") {
          val m1 = Measurement(1.0, MeasurementUnit.FluidOunce)
          val m2 = Measurement(2.0, MeasurementUnit.Tablespoon)
          (m1 == m2) shouldBe true
        }
      }

      context("Inequality") {
        test("Teaspoon & Tablespoon") {
          val m1 = Measurement(1.0, MeasurementUnit.Teaspoon)
          val m2 = Measurement(1.0, MeasurementUnit.Tablespoon)
          (m1 != m2) shouldBe true
        }

        test("Cup & Fluid Ounces") {
          val m1 = Measurement(1.0, MeasurementUnit.Cup)
          val m2 = Measurement(1.0, MeasurementUnit.FluidOunce)
          (m1 != m2) shouldBe true
        }

        test("Fluid Ounces & Tablespoons") {
          val m1 = Measurement(1.0, MeasurementUnit.FluidOunce)
          val m2 = Measurement(1.0, MeasurementUnit.Tablespoon)
          (m1 != m2) shouldBe true
        }
      }

      context("Greater Than") {
        test("Greater") {
          val m1 = Measurement(1.0, MeasurementUnit.Tablespoon)
          val m2 = Measurement(1.0, MeasurementUnit.Teaspoon)
          (m1 > m2) shouldBe true
        }

        test("Not Greater") {
          val m1 = Measurement(1.0, MeasurementUnit.Teaspoon)
          val m2 = Measurement(1.0, MeasurementUnit.Tablespoon)
          (m1 > m2) shouldBe false
        }

        test("Or Equal To") {
          val m1 = Measurement(3.0, MeasurementUnit.Teaspoon)
          val m2 = Measurement(1.0, MeasurementUnit.Tablespoon)
          (m1 >= m2) shouldBe true
        }
      }

      context("Less Than") {
        test("Less") {
          val m1 = Measurement(1.0, MeasurementUnit.Tablespoon)
          val m2 = Measurement(1.0, MeasurementUnit.Teaspoon)
          (m2 < m1) shouldBe true
        }

        test("Not") {
          val m1 = Measurement(1.0, MeasurementUnit.Teaspoon)
          val m2 = Measurement(1.0, MeasurementUnit.Tablespoon)
          (m2 < m1) shouldBe false
        }

        test("Or Equal To") {
          val m1 = Measurement(3.0, MeasurementUnit.Teaspoon)
          val m2 = Measurement(1.0, MeasurementUnit.Tablespoon)
          (m2 <= m1) shouldBe true
        }
      }
    }
  }

  context("Arithmetic") {
    context("Addition") {
      test("Teaspoon + Tablespoon") {
        val m1 = Measurement(1.0, MeasurementUnit.Teaspoon)
        val m2 = Measurement(1.0, MeasurementUnit.Tablespoon)
        val sum = m1 + m2
        sum.quantity shouldBe (4.0 plusOrMinus 0.001)
        sum.unit shouldBe MeasurementUnit.Teaspoon
      }

      test("Tablespoon + Cup") {
        val m1 = Measurement(8.0, MeasurementUnit.Tablespoon)
        val m2 = Measurement(1.0, MeasurementUnit.Cup)
        val sum = m1 + m2
        sum.quantity shouldBe (24.0 plusOrMinus 0.001)
        sum.unit shouldBe MeasurementUnit.Tablespoon
      }

      test("Cup + Fluid Ounces") {
        val m1 = Measurement(1.0, MeasurementUnit.Cup)
        val m2 = Measurement(8.0, MeasurementUnit.FluidOunce)
        val sum = m1 + m2
        sum.quantity shouldBe (2.0 plusOrMinus 0.001)
        sum.unit shouldBe MeasurementUnit.Cup
      }


    }

    context("Subtraction") {
      test("Teaspoon - Tablespoon") {
        val m1 = Measurement(4.0, MeasurementUnit.Teaspoon)
        val m2 = Measurement(1.0, MeasurementUnit.Tablespoon)
        val diff = m1 - m2
        diff.quantity shouldBe (1.0 plusOrMinus 0.001)
        diff.unit shouldBe MeasurementUnit.Teaspoon
      }

      test("Cup - Tablespoon") {
        val m1 = Measurement(2.0, MeasurementUnit.Cup)
        val m2 = Measurement(8.0, MeasurementUnit.Tablespoon)
        val diff = m1 - m2
        diff.quantity shouldBe (1.5 plusOrMinus 0.001)
        diff.unit shouldBe MeasurementUnit.Cup
      }

      test("Cup - Fluid Ounces") {
        val m1 = Measurement(2.0, MeasurementUnit.Cup)
        val m2 = Measurement(8.0, MeasurementUnit.FluidOunce)
        val diff = m1 - m2
        diff.quantity shouldBe (1.0 plusOrMinus 0.001)
        diff.unit shouldBe MeasurementUnit.Cup
      }
    }
  }
})