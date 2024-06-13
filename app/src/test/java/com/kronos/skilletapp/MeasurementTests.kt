package com.kronos.skilletapp

import com.kronos.skilletapp.model.Measurement
import com.kronos.skilletapp.model.MeasurementUnit
import io.kotest.assertions.shouldFail
import io.kotest.assertions.throwables.shouldThrow
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
      scaled.amount shouldBe (20.0 plusOrMinus 0.001)
    }

    test("Triple") {
      val scaled = grams.scale(3.0)
      scaled.amount shouldBe (30.0 plusOrMinus 0.001)
    }

    test("Zero") {
      val scaled = grams.scale(0.0)
      scaled.amount shouldBe (0.0 plusOrMinus 0.001)
    }

    test("Identity") {
      val scaled = grams.scale(1.0)
      scaled.amount shouldBe (10.0 plusOrMinus 0.001)
    }

    test("Negative") {
      val scaled = grams.scale(-1.0)
      scaled.amount shouldBe (-10.0 plusOrMinus 0.001)
    }

    test("Half") {
      val scaled = grams.scale(0.5)
      scaled.amount shouldBe (5.0 plusOrMinus 0.001)
    }

    test("Quarter") {
      val scaled = grams.scale(0.25)
      scaled.amount shouldBe (2.5 plusOrMinus 0.001)
    }
  }

  context("Conversions") {
    context("Mass") {
      context("Metric") {
        test("Grams to Kilograms") {
          val converted = grams.convert(MeasurementUnit.Kilogram)
          converted.amount shouldBe (0.01 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Kilogram
        }

        test("Kilograms to Grams") {
          val converted = kilogram.convert(MeasurementUnit.Gram)
          converted.amount shouldBe (1000.0 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Gram
        }
      }

      context("Imperial") {
        test("Ounces to Pounds") {
          val converted = ounces.convert(MeasurementUnit.Pound)
          converted.amount shouldBe (1.0 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Pound
        }

        test("Pounds to Ounces") {
          val converted = pound.convert(MeasurementUnit.Ounce)
          converted.amount shouldBe (16.0 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Ounce
        }
      }

      context("Metric to Imperial") {
        test("Grams to Ounces") {
          val converted = grams.convert(MeasurementUnit.Ounce)
          converted.amount shouldBe (0.35274 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Ounce
        }

        test("Kilograms to Pounds") {
          val converted = kilogram.convert(MeasurementUnit.Pound)
          converted.amount shouldBe (2.20462 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Pound
        }
      }

      context("Imperial to Metric") {
        test("Ounces to Grams") {
          val converted = ounces.convert(MeasurementUnit.Gram)
          converted.amount shouldBe (453.592 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Gram
        }

        test("Pounds to Kilograms") {
          val converted = pound.convert(MeasurementUnit.Kilogram)
          converted.amount shouldBe (0.453592 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Kilogram
        }
      }
    }

    context("Volume") {
      context("Metric") {
        test("Milliliter to Liter") {
          val converted = milliliters.convert(MeasurementUnit.Liter)
          converted.amount shouldBe (0.1 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Liter
        }

        test("Liter to Milliliter") {
          val converted = liter.convert(MeasurementUnit.Milliliter)
          converted.amount shouldBe (1000.0 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Milliliter
        }
      }

      context("Imperial") {
        test("Gallons to Quarts") {
          val converted = gallon.convert(MeasurementUnit.Quart)
          converted.amount shouldBe (4.0 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Quart
        }

        test("Quarts to Gallons") {
          val converted = quart.convert(MeasurementUnit.Gallon)
          converted.amount shouldBe (0.25 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Gallon
        }

        test("Quarts to Pints") {
          val converted = quart.convert(MeasurementUnit.Pint)
          converted.amount shouldBe (2.0 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Pint
        }

        test("Pints to Quarts") {
          val converted = pint.convert(MeasurementUnit.Quart)
          converted.amount shouldBe (0.5 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Quart
        }

        test("Cups to Tablespoons") {
          val converted = cup.convert(MeasurementUnit.Tablespoon)
          converted.amount shouldBe (16.0 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Tablespoon
        }

        test("Tablespoons to Cups") {
          val converted = tablespoon.convert(MeasurementUnit.Cup)
          converted.amount shouldBe (0.0625 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Cup
        }

        test("Cups to Fluid Ounces") {
          val converted = cup.convert(MeasurementUnit.FluidOunce)
          converted.amount shouldBe (8.0 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.FluidOunce
        }

        test("Fluid Ounces to Cups") {
          val converted = fluidOunce.convert(MeasurementUnit.Cup)
          converted.amount shouldBe (0.125 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Cup
        }

        test("Cups to Fluid Ounces") {
          val converted = cup.convert(MeasurementUnit.FluidOunce)
          converted.amount shouldBe (8.0 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.FluidOunce
        }

        test("Fluid Ounces to Tablespoons") {
          val converted = fluidOunce.convert(MeasurementUnit.Tablespoon)
          converted.amount shouldBe (2.0 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Tablespoon
        }

        test("Tablespoons to Fluid Ounces") {
          val converted = tablespoon.convert(MeasurementUnit.FluidOunce)
          converted.amount shouldBe (0.5 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.FluidOunce
        }

        test("Tablespoons to Teaspoons") {
          val converted = tablespoon.convert(MeasurementUnit.Teaspoon)
          converted.amount shouldBe (3.0 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Teaspoon
        }

        test("Teaspoons to Tablespoons") {
          val converted = teaspoon.convert(MeasurementUnit.Tablespoon)
          converted.amount shouldBe (0.333 plusOrMinus 0.001)
          converted.unit shouldBe MeasurementUnit.Tablespoon
        }
      }
    }

    context("Metric to Imperial") {
      test("Milliliters to Fluid Ounces") {
        val converted = milliliters.convert(MeasurementUnit.FluidOunce)
        converted.amount shouldBe (3.381 plusOrMinus 0.001)
        converted.unit shouldBe MeasurementUnit.FluidOunce
      }

      test("Milliliters to Cups") {
        val converted = milliliters.convert(MeasurementUnit.Cup)
        converted.amount shouldBe (0.4232 plusOrMinus 0.001)
        converted.unit shouldBe MeasurementUnit.Cup
      }
    }

    context("Imperial to Metric") {
      test("Tablespoons to Milliliters") {
        val converted = tablespoon.convert(MeasurementUnit.Milliliter)
        converted.amount shouldBe (14.7868 plusOrMinus 0.001)
        converted.unit shouldBe MeasurementUnit.Milliliter
      }

      test("Cups to Milliliters") {
        val converted = cup.convert(MeasurementUnit.Milliliter)
        converted.amount shouldBe (236.588 plusOrMinus 0.001)
        converted.unit shouldBe MeasurementUnit.Milliliter
      }
    }
  }

  context("Custom Conversions") {
    test("Volume to Mass") {
      val tablespoonsButter = Measurement(2.0, MeasurementUnit.Tablespoon)
      val gramsButter = tablespoonsButter.convert(MeasurementUnit.Gram) { it * 14 }

      gramsButter.amount shouldBe (28.0 plusOrMinus 0.001)
      gramsButter.unit shouldBe MeasurementUnit.Gram
    }

    test("Mass to Volume") {
      val gramsButter = Measurement(28.0, MeasurementUnit.Gram)
      val tablespoonsButter = gramsButter.convert(MeasurementUnit.Tablespoon) { it / 14 }

      tablespoonsButter.amount shouldBe (2.0 plusOrMinus 0.001)
      tablespoonsButter.unit shouldBe MeasurementUnit.Tablespoon
    }
  }

  context("Improper Conversions") {
    test("Improper Volume to Mass") {
      shouldThrow<IllegalStateException> {
        teaspoon.convert(MeasurementUnit.Gram)
      }
    }

    test("Improper Mass to Volume") {
      shouldThrow<IllegalStateException> {
        grams.convert(MeasurementUnit.Teaspoon)
      }
    }
  }
})