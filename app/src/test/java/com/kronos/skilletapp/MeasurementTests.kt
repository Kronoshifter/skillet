package com.kronos.skilletapp

import com.kronos.skilletapp.model.measurement.Measurement
import com.kronos.skilletapp.model.measurement.MeasurementUnit
import com.kronos.skilletapp.model.measurement.convertBy
import com.kronos.skilletapp.model.measurement.convertTo
import com.kronos.skilletapp.model.measurement.converter
import com.kronos.skilletapp.model.measurement.isEquivalentTo
import com.kronos.skilletapp.model.measurement.of
import com.kronos.skilletapp.model.measurement.withConverter
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
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
          converted.unit shouldBe MeasurementUnit.Kilogram
          converted.quantity shouldBe (0.01f plusOrMinus 0.001f)
        }

        test("Kilograms to Grams") {
          val converted = kilogram convertTo MeasurementUnit.Gram
          converted.unit shouldBe MeasurementUnit.Gram
          converted.quantity shouldBe (1000f plusOrMinus 0.001f)
        }
      }

      context("Imperial") {
        test("Ounces to Pounds") {
          val converted = ounces convertTo MeasurementUnit.Pound
          converted.unit shouldBe MeasurementUnit.Pound
          converted.quantity shouldBe (1f plusOrMinus 0.001f)
        }

        test("Pounds to Ounces") {
          val converted = pound convertTo MeasurementUnit.Ounce
          converted.unit shouldBe MeasurementUnit.Ounce
          converted.quantity shouldBe (16f plusOrMinus 0.001f)
        }
      }

      context("Metric to Imperial") {
        test("Grams to Ounces") {
          val converted = grams convertTo MeasurementUnit.Ounce
          converted.unit shouldBe MeasurementUnit.Ounce
          converted.quantity shouldBe (0.35274f plusOrMinus 0.001f)
        }

        test("Kilograms to Pounds") {
          val converted = kilogram convertTo MeasurementUnit.Pound
          converted.unit shouldBe MeasurementUnit.Pound
          converted.quantity shouldBe (2.20462f plusOrMinus 0.001f)
        }
      }

      context("Imperial to Metric") {
        test("Ounces to Grams") {
          val converted = ounces convertTo MeasurementUnit.Gram
          converted.unit shouldBe MeasurementUnit.Gram
          converted.quantity shouldBe (453.592f plusOrMinus 0.001f)
        }

        test("Pounds to Kilograms") {
          val converted = pound convertTo MeasurementUnit.Kilogram
          converted.unit shouldBe MeasurementUnit.Kilogram
          converted.quantity shouldBe (0.453592f plusOrMinus 0.001f)
        }
      }
    }

    context("Volume") {
      context("Metric") {
        test("Milliliter to Liter") {
          val converted = milliliters convertTo MeasurementUnit.Liter
          converted.unit shouldBe MeasurementUnit.Liter
          converted.quantity shouldBe (0.1f plusOrMinus 0.001f)
        }

        test("Liter to Milliliter") {
          val converted = liter convertTo MeasurementUnit.Milliliter
          converted.unit shouldBe MeasurementUnit.Milliliter
          converted.quantity shouldBe (1000f plusOrMinus 0.001f)
        }
      }

      context("Imperial") {
        test("Gallons to Quarts") {
          val converted = gallon convertTo MeasurementUnit.Quart
          converted.unit shouldBe MeasurementUnit.Quart
          converted.quantity shouldBe (4f plusOrMinus 0.001f)
        }

        test("Quarts to Gallons") {
          val converted = quart convertTo MeasurementUnit.Gallon
          converted.unit shouldBe MeasurementUnit.Gallon
          converted.quantity shouldBe (0.25f plusOrMinus 0.001f)
        }

        test("Quarts to Pints") {
          val converted = quart convertTo MeasurementUnit.Pint
          converted.unit shouldBe MeasurementUnit.Pint
          converted.quantity shouldBe (2f plusOrMinus 0.001f)
        }

        test("Pints to Quarts") {
          val converted = pint convertTo MeasurementUnit.Quart
          converted.unit shouldBe MeasurementUnit.Quart
          converted.quantity shouldBe (0.5f plusOrMinus 0.001f)
        }

        test("Cups to Tablespoons") {
          val converted = cup convertTo MeasurementUnit.Tablespoon
          converted.unit shouldBe MeasurementUnit.Tablespoon
          converted.quantity shouldBe (16f plusOrMinus 0.001f)
        }

        test("Tablespoons to Cups") {
          val converted = tablespoon convertTo MeasurementUnit.Cup
          converted.unit shouldBe MeasurementUnit.Cup
          converted.quantity shouldBe (0.0625f plusOrMinus 0.001f)
        }

        test("Cups to Fluid Ounces") {
          val converted = cup convertTo MeasurementUnit.FluidOunce
          converted.unit shouldBe MeasurementUnit.FluidOunce
          converted.quantity shouldBe (8f plusOrMinus 0.001f)
        }

        test("Fluid Ounces to Cups") {
          val converted = fluidOunce convertTo MeasurementUnit.Cup
          converted.unit shouldBe MeasurementUnit.Cup
          converted.quantity shouldBe (0.125f plusOrMinus 0.001f)
        }

        test("Fluid Ounces to Tablespoons") {
          val converted = fluidOunce convertTo MeasurementUnit.Tablespoon
          converted.unit shouldBe MeasurementUnit.Tablespoon
          converted.quantity shouldBe (2f plusOrMinus 0.001f)
        }

        test("Tablespoons to Fluid Ounces") {
          val converted = tablespoon convertTo MeasurementUnit.FluidOunce
          converted.unit shouldBe MeasurementUnit.FluidOunce
          converted.quantity shouldBe (0.5f plusOrMinus 0.001f)
        }

        test("Tablespoons to Teaspoons") {
          val converted = tablespoon convertTo MeasurementUnit.Teaspoon
          converted.unit shouldBe MeasurementUnit.Teaspoon
          converted.quantity shouldBe (3f plusOrMinus 0.001f)
        }

        test("Teaspoons to Tablespoons") {
          val converted = teaspoon convertTo MeasurementUnit.Tablespoon
          converted.unit shouldBe MeasurementUnit.Tablespoon
          converted.quantity shouldBe (0.333f plusOrMinus 0.001f)
        }
      }

      context("Metric to Imperial") {
        test("Milliliters to Fluid Ounces") {
          val converted = milliliters convertTo MeasurementUnit.FluidOunce
          converted.unit shouldBe MeasurementUnit.FluidOunce
          converted.quantity shouldBe (3.381f plusOrMinus 0.001f)
        }

        test("Milliliters to Cups") {
          val converted = milliliters convertTo MeasurementUnit.Cup
          converted.unit shouldBe MeasurementUnit.Cup
          converted.quantity shouldBe (0.4232f plusOrMinus 0.001f)
        }
      }

      context("Imperial to Metric") {
        test("Tablespoons to Milliliters") {
          val converted = tablespoon convertTo MeasurementUnit.Milliliter
          converted.unit shouldBe MeasurementUnit.Milliliter
          converted.quantity shouldBe (14.7868f plusOrMinus 0.001f)
        }

        test("Cups to Milliliters") {
          val converted = cup convertTo MeasurementUnit.Milliliter
          converted.unit shouldBe MeasurementUnit.Milliliter
          converted.quantity shouldBe (236.588f plusOrMinus 0.001f)
        }
      }
    }

    context("Cross-Dimension Conversions") {
      context("Measurement.convertBy") {
        test("Volume to Mass") {
          val tablespoonsButter = Measurement(2f, MeasurementUnit.Tablespoon)
          val gramsButter = tablespoonsButter.convertBy {
            (1 of MeasurementUnit.Tablespoon) to (14 of MeasurementUnit.Gram)
          }
  
          gramsButter.unit shouldBe MeasurementUnit.Gram
          gramsButter.quantity shouldBe (28f plusOrMinus 0.001f)
        }

        test("Mass to Volume") {
          val gramsButter = Measurement(28f, MeasurementUnit.Gram)
          val tablespoonsButter = gramsButter.convertBy {
            (14 of MeasurementUnit.Gram) to (1 of MeasurementUnit.Tablespoon)
          }
  
          tablespoonsButter.unit shouldBe MeasurementUnit.Tablespoon
          tablespoonsButter.quantity shouldBe (2f plusOrMinus 0.001f)
        }

        test("Custom to Mass") {
          val garlic = Measurement(1f, MeasurementUnit.Custom("cloves"))
          val gramsGarlic = garlic.convertBy {
            garlic to (3 of MeasurementUnit.Gram)
          }
  
          gramsGarlic.unit shouldBe MeasurementUnit.Gram
          gramsGarlic.quantity shouldBe (3f plusOrMinus 0.001f)
        }

        test("Custom to Volume") {
          val unit = MeasurementUnit.Custom("cloves")
          val garlic = Measurement(2f, unit)
          val gramsGarlic = garlic.convertBy {
            (1 of unit) to (0.5f of MeasurementUnit.Teaspoon)
          }
  
          gramsGarlic.unit shouldBe MeasurementUnit.Teaspoon
          gramsGarlic.quantity shouldBe (1f plusOrMinus 0.001f)
        }

        test("None to mass") {
          val coconut = Measurement(1f, MeasurementUnit.None)
          val gramsCoconut = coconut.convertBy {
            coconut to (400 of MeasurementUnit.Gram)
          }
  
          gramsCoconut.unit shouldBe MeasurementUnit.Gram
          gramsCoconut.quantity shouldBe (400f plusOrMinus 0.001f)
        }

        test("None to volume") {
          val coconut = Measurement(2f, MeasurementUnit.None)
          val quarterCupCoconut = coconut.convertBy {
            (1 of MeasurementUnit.None) to (0.25f of MeasurementUnit.Cup)
          }
  
          quarterCupCoconut.unit shouldBe MeasurementUnit.Cup
          quarterCupCoconut.quantity shouldBe (0.5f plusOrMinus 0.001f)
        }
      }
      
      context("Measurement.convertTo") {
        test("Volume to Mass") {
          val tablespoonsButter = Measurement(2f, MeasurementUnit.Tablespoon)
          val gramsButter = tablespoonsButter convertTo (28 of MeasurementUnit.Gram)

          gramsButter.unit shouldBe MeasurementUnit.Gram
          gramsButter.quantity shouldBe (28f plusOrMinus 0.001f)
        }
        
        test("Mass to Volume") {
          val gramsButter = Measurement(28f, MeasurementUnit.Gram)
          val tablespoonsButter = gramsButter convertTo (2 of MeasurementUnit.Tablespoon)

          tablespoonsButter.unit shouldBe MeasurementUnit.Tablespoon
          tablespoonsButter.quantity shouldBe (2f plusOrMinus 0.001f)
        }

        test("Custom to Mass") {
          val garlic = Measurement(1f, MeasurementUnit.Custom("cloves"))
          val gramsGarlic = garlic convertTo (3 of MeasurementUnit.Gram)

          gramsGarlic.unit shouldBe MeasurementUnit.Gram
          gramsGarlic.quantity shouldBe (3f plusOrMinus 0.001f)
        }

        test("Custom to Volume") {
          val unit = MeasurementUnit.Custom("cloves")
          val clovesGarlic = Measurement(2f, unit)
          val teaspoonsGarlic = clovesGarlic convertTo (1f of MeasurementUnit.Teaspoon)

          teaspoonsGarlic.unit shouldBe MeasurementUnit.Teaspoon
          teaspoonsGarlic.quantity shouldBe (1f plusOrMinus 0.001f)
        }

        test("None to mass") {
          val coconut = Measurement(1f, MeasurementUnit.None)
          val gramsCoconut = coconut convertTo (400 of MeasurementUnit.Gram)

          gramsCoconut.unit shouldBe MeasurementUnit.Gram
          gramsCoconut.quantity shouldBe (400f plusOrMinus 0.001f)
        }

        test("None to volume") {
          val coconut = Measurement(1f, MeasurementUnit.None)
          val quarterCupCoconut = coconut convertTo (0.25f of MeasurementUnit.Cup)

          quarterCupCoconut.unit shouldBe MeasurementUnit.Cup
          quarterCupCoconut.quantity shouldBe (0.25f plusOrMinus 0.001f)
        }
      }

      context("withConverter") {
        test("Volume to Mass") {
          val tablespoonsButter = Measurement(2f, MeasurementUnit.Tablespoon)
          val gramsButter = withConverter({ (1 of MeasurementUnit.Tablespoon) to (14 of MeasurementUnit.Gram) }) {
            tablespoonsButter convertTo MeasurementUnit.Gram
          }

          gramsButter.unit shouldBe MeasurementUnit.Gram
          gramsButter.quantity shouldBe (28f plusOrMinus 0.001f)
        }


        test("Mass to Volume") {
          val gramsButter = Measurement(28f, MeasurementUnit.Gram)
          val tablespoonsButter = withConverter({ (14 of MeasurementUnit.Gram) to (1 of MeasurementUnit.Tablespoon) }) {
            gramsButter convertTo MeasurementUnit.Tablespoon
          }

          tablespoonsButter.unit shouldBe MeasurementUnit.Tablespoon
          tablespoonsButter.quantity shouldBe (2f plusOrMinus 0.001f)
        }

        test("Custom to Mass") {
          val garlic = Measurement(1f, MeasurementUnit.Custom("cloves"))
          val gramsGarlic = withConverter({ (1 of MeasurementUnit.Custom("cloves")) to (3 of MeasurementUnit.Gram) }) {
            garlic convertTo MeasurementUnit.Gram
          }

          gramsGarlic.unit shouldBe MeasurementUnit.Gram
          gramsGarlic.quantity shouldBe (3f plusOrMinus 0.001f)
        }

        test("Custom to Volume") {
          val unit = MeasurementUnit.Custom("cloves")
          val clovesGarlic = Measurement(2f, unit)
          val teaspoonsGarlic = withConverter({ (1 of unit) to (0.5 of MeasurementUnit.Teaspoon) }) {
            clovesGarlic convertTo MeasurementUnit.Teaspoon
          }

          teaspoonsGarlic.unit shouldBe MeasurementUnit.Teaspoon
          teaspoonsGarlic.quantity shouldBe (1f plusOrMinus 0.001f)
        }

        test("None to mass") {
          val coconut = Measurement(1f, MeasurementUnit.None)
          val gramsCoconut = withConverter({ coconut to (400 of MeasurementUnit.Gram) }) {
            coconut convertTo MeasurementUnit.Gram
          }

          gramsCoconut.unit shouldBe MeasurementUnit.Gram
          gramsCoconut.quantity shouldBe (400f plusOrMinus 0.001f)
        }

        test("None to volume") {
          val coconut = Measurement(2f, MeasurementUnit.None)
          val quarterCupCoconut = withConverter({ (1f of MeasurementUnit.None) to (0.25f of MeasurementUnit.Cup) }) {
            coconut convertTo MeasurementUnit.Cup
          }

          quarterCupCoconut.unit shouldBe MeasurementUnit.Cup
          quarterCupCoconut.quantity shouldBe (0.5f plusOrMinus 0.001f)
        }
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
        normalized.unit shouldBe MeasurementUnit.None
        normalized.quantity shouldBe 0f
      }
    }
  }

  context("Normalizing") {

    context("Teaspoon") {
      test("Teaspoon to Tablespoon") {
        val normalized = Measurement(3f, MeasurementUnit.Teaspoon).normalized()
        normalized.unit shouldBe MeasurementUnit.Tablespoon
        normalized.quantity shouldBe (1f plusOrMinus 0.001f)
      }

      test("Teaspoon to Cup") {
        val normalized = Measurement(48f, MeasurementUnit.Teaspoon).normalized()
        normalized.unit shouldBe MeasurementUnit.Cup
        normalized.quantity shouldBe (1f plusOrMinus 0.001f)
      }

      test("Cup to Tablespoon") {
        val normalized = Measurement(1f / 16f, MeasurementUnit.Cup).normalized()
        normalized.unit shouldBe MeasurementUnit.Tablespoon
        normalized.quantity shouldBe (1f plusOrMinus 0.001f)
      }
    }
  }

  context("Scaling and Normalizing") {
    context("Metric") {

    }

    context("Imperial") {
      test("Teaspoon to Tablespoon") {
        val scaled = teaspoon.scale(3f).normalized().roundToEighth()
        scaled.unit shouldBe MeasurementUnit.Tablespoon
        scaled.quantity shouldBe (1f plusOrMinus 0.001f)
      }

      test("Tablespoon to Teaspoon") {
        val scaled = tablespoon.scale(0.333f).normalized().roundToEighth()
        scaled.unit shouldBe MeasurementUnit.Teaspoon
        scaled.quantity shouldBe (1f plusOrMinus 0.001f)
      }

      test("Tablespoon to Cup") {
        val scaled = tablespoon.scale(24f).normalized().roundToEighth()
        scaled.unit shouldBe MeasurementUnit.Cup
        scaled.quantity shouldBe (1.5f plusOrMinus 0.001f)
      }
    }
  }

  context("Scale and Round") {
    test("0.90834298 * 23") {
      val m = Measurement(0.90834298f, MeasurementUnit.Milliliter)
      val scaled = m.scale(23f).roundToEighth()
      scaled.unit shouldBe MeasurementUnit.Milliliter
      scaled.quantity shouldBe (20.875f plusOrMinus 0.001f)
    }

    test("10.0624f * 1.5") {
      val m = Measurement(10.0624f, MeasurementUnit.Milliliter)
      val scaled = m.scale(1.5f).roundToEighth()
      scaled.unit shouldBe MeasurementUnit.Milliliter
      scaled.quantity shouldBe (15.125f plusOrMinus 0.001f)
    }

    test(".125 / 2") {
      val m = Measurement(0.125f, MeasurementUnit.Cup)
      val scaled = m.scale(0.5f).roundToEighth()
      scaled.unit shouldBe MeasurementUnit.Cup
      scaled.quantity shouldBe (0.125f plusOrMinus 0.001f)
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
        test("Teaspoon") {
          val m1 = Measurement(1f, MeasurementUnit.Teaspoon)
          val m2 = Measurement(1f, MeasurementUnit.Tablespoon)
          (m1 == m2) shouldBe false
        }

        test("Fluid Ounces & Tablespoons") {
          val m1 = Measurement(1f, MeasurementUnit.FluidOunce)
          val m2 = Measurement(1f, MeasurementUnit.Tablespoon)
          (m1 == m2) shouldBe false
        }

        test("Cup & Fluid Ounces") {
          val m1 = Measurement(1f, MeasurementUnit.Cup)
          val m2 = Measurement(1f, MeasurementUnit.FluidOunce)
          (m1 == m2) shouldBe false
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
          val m2 = Measurement(1f, MeasurementUnit.Teaspoon)
          (m2 <= m1) shouldBe true
        }
      }
    }
  }

  context("Equivalence") {
    test("isEquivalentTo returns true when both measurements are none") {
      val measurement1 = Measurement.None
      val measurement2 = Measurement.None
      (measurement1 isEquivalentTo measurement2) shouldBe true
    }


    test("isEquivalentTo returns true when same custom unit and equal quantities") {
      val measurement1 = Measurement(1f, MeasurementUnit.Custom("foo"))
      val measurement2 = Measurement(1f, MeasurementUnit.Custom("foo"))
      (measurement1 isEquivalentTo measurement2) shouldBe true
    }

    test("isEquivalentTo returns false when same custom unit but different quantities") {
      val measurement1 = Measurement(1f, MeasurementUnit.Custom("foo"))
      val measurement2 = Measurement(2f, MeasurementUnit.Custom("foo"))
      (measurement1 isEquivalentTo measurement2) shouldBe false
    }

    test("isEquivalentTo returns false when different custom units") {
      val measurement1 = Measurement(1f, MeasurementUnit.Custom("foo"))
      val measurement2 = Measurement(1f, MeasurementUnit.Custom("bar"))
      (measurement1 isEquivalentTo measurement2) shouldBe false
    }


    test("isEquivalentTo returns false when only one measurement has a custom unit") {
      val measurement1 = Measurement(1f, MeasurementUnit.Gram)
      val measurement2 = Measurement(1f, MeasurementUnit.Custom("foo"))
      (measurement1 isEquivalentTo measurement2) shouldBe false
    }

    test("isEquivalentTo returns false when only one measurement is none") {
      val measurement1 = Measurement.None
      val measurement2 = Measurement(1f, MeasurementUnit.Gram)
      (measurement1 isEquivalentTo measurement2) shouldBe false
    }

    test("isEquivalentTo returns true when same unit and equal quantities") {
      val measurement1 = Measurement(1f, MeasurementUnit.Gram)
      val measurement2 = Measurement(1f, MeasurementUnit.Gram)
      (measurement1 isEquivalentTo measurement2) shouldBe true
    }

    test("isEquivalentTo returns false when same unit but different quantities") {
      val measurement1 = Measurement(1f, MeasurementUnit.Gram)
      val measurement2 = Measurement(2f, MeasurementUnit.Gram)
      (measurement1 isEquivalentTo measurement2) shouldBe false
    }

    test("isEquivalentTo returns true when same dimension but different units") {
      val measurement1 = Measurement(1000f, MeasurementUnit.Gram)
      val measurement2 = Measurement(1f, MeasurementUnit.Kilogram)
      (measurement1 isEquivalentTo measurement2) shouldBe true
    }

    test("isEquivalentTo returns false when same dimension but different units and quantities") {
      val measurement1 = Measurement(1f, MeasurementUnit.Gram)
      val measurement2 = Measurement(0.5f, MeasurementUnit.Kilogram)
      (measurement1 isEquivalentTo measurement2) shouldBe false
    }

    test("isEquivalentTo returns false when units are incompatible") {
      val measurement1 = Measurement(1f, MeasurementUnit.Gram)
      val measurement2 = Measurement(1f, MeasurementUnit.Liter)
      (measurement1 isEquivalentTo measurement2) shouldBe false
    }
  }

  context("Arithmetic") {
    context("Addition") {
      test("Teaspoon + Tablespoon") {
        val m1 = Measurement(1f, MeasurementUnit.Teaspoon)
        val m2 = Measurement(1f, MeasurementUnit.Tablespoon)
        val sum = m1 + m2
        sum.unit shouldBe MeasurementUnit.Teaspoon
        sum.quantity shouldBe (4f plusOrMinus 0.001f)
      }

      test("Tablespoon + Cup") {
        val m1 = Measurement(8f, MeasurementUnit.Tablespoon)
        val m2 = Measurement(1f, MeasurementUnit.Cup)
        val sum = m1 + m2
        sum.unit shouldBe MeasurementUnit.Tablespoon
        sum.quantity shouldBe (24f plusOrMinus 0.001f)
      }

      test("Cup + Fluid Ounces") {
        val m1 = Measurement(1f, MeasurementUnit.Cup)
        val m2 = Measurement(8f, MeasurementUnit.FluidOunce)
        val sum = m1 + m2
        sum.unit shouldBe MeasurementUnit.Cup
        sum.quantity shouldBe (2f plusOrMinus 0.001f)
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
        diff.unit shouldBe MeasurementUnit.Teaspoon
        diff.quantity shouldBe (1f plusOrMinus 0.001f)
      }

      test("Cup - Tablespoon") {
        val m1 = Measurement(2f, MeasurementUnit.Cup)
        val m2 = Measurement(8f, MeasurementUnit.Tablespoon)
        val diff = m1 - m2
        diff.unit shouldBe MeasurementUnit.Cup
        diff.quantity shouldBe (1.5f plusOrMinus 0.001f)
      }

      test("Cup - Fluid Ounces") {
        val m1 = Measurement(2f, MeasurementUnit.Cup)
        val m2 = Measurement(8f, MeasurementUnit.FluidOunce)
        val diff = m1 - m2
        diff.unit shouldBe MeasurementUnit.Cup
        diff.quantity shouldBe (1f plusOrMinus 0.001f)
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

    context("Multiplication") {
      test("Int") {
        val m = (teaspoon * 3)
        m.unit shouldBe MeasurementUnit.Teaspoon
        m.quantity shouldBe (3f plusOrMinus 0.001f)
      }

      test("Float") {
        val m = (teaspoon * 3f)
        m.unit shouldBe MeasurementUnit.Teaspoon
        m.quantity shouldBe (3f plusOrMinus 0.001f)
      }

      test("Double") {
        val m = (teaspoon * 3.0)
        m.unit shouldBe MeasurementUnit.Teaspoon
        m.quantity shouldBe (3f plusOrMinus 0.001f)
      }

      test("Long") {
        val m = (teaspoon * 3L)
        m.unit shouldBe MeasurementUnit.Teaspoon
        m.quantity shouldBe (3f plusOrMinus 0.001f)
      }

      test("Short") {
        val m = (teaspoon * 3.toShort())
        m.unit shouldBe MeasurementUnit.Teaspoon
        m.quantity shouldBe (3f plusOrMinus 0.001f)
      }

      test("Byte") {
        val m = (teaspoon * 3.toByte())
        m.unit shouldBe MeasurementUnit.Teaspoon
        m.quantity shouldBe (3f plusOrMinus 0.001f)
      }
    }

    context("Division") {
      test("Int") {
        val m = (teaspoon / 3)
        m.unit shouldBe MeasurementUnit.Teaspoon
        m.quantity shouldBe (1f / 3f plusOrMinus 0.001f)
      }

      test("Float") {
        val m = (teaspoon / 3f)
        m.unit shouldBe MeasurementUnit.Teaspoon
        m.quantity shouldBe (1f / 3f plusOrMinus 0.001f)
      }

      test("Double") {
        val m = (teaspoon / 3.0)
        m.unit shouldBe MeasurementUnit.Teaspoon
        m.quantity shouldBe (1f / 3f plusOrMinus 0.001f)
      }

      test("Long") {
        val m = (teaspoon / 3L)
        m.unit shouldBe MeasurementUnit.Teaspoon
        m.quantity shouldBe (1f / 3f plusOrMinus 0.001f)
      }

      test("Short") {
        val m = (teaspoon / 3.toShort())
        m.unit shouldBe MeasurementUnit.Teaspoon
        m.quantity shouldBe (1f / 3f plusOrMinus 0.001f)
      }

      test("Byte") {
        val m = (teaspoon / 3.toByte())
        m.unit shouldBe MeasurementUnit.Teaspoon
        m.quantity shouldBe (1f / 3f plusOrMinus 0.001f)
      }
    }
  }
})