package com.kronos.skilletapp

import com.kronos.skilletapp.model.MeasurementUnit
import com.kronos.skilletapp.parser.IngredientParser
import io.kotest.core.project.projectContext
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class ParserTests : FunSpec({

  context("Parser Tests") {
    context("Decimal Quantity") {
      test("1 cup") {
        val ingredient = IngredientParser.parseIngredient("1 cup butter\n")
        ingredient.measurement.unit shouldBe MeasurementUnit.Cup
        ingredient.measurement.quantity shouldBe (1.0 plusOrMinus 0.01)
      }

      test("1.5 cup") {
        val ingredient = IngredientParser.parseIngredient("1.5 cup butter\n")
        ingredient.measurement.unit shouldBe MeasurementUnit.Cup
        ingredient.measurement.quantity shouldBe (1.5 plusOrMinus 0.01)
      }
    }

    context("Fractional Quantity") {
      test("1/2 cup") {
        val ingredient = IngredientParser.parseIngredient("1/2 cup butter\n")
        ingredient.measurement.unit shouldBe MeasurementUnit.Cup
        ingredient.measurement.quantity shouldBe (0.5 plusOrMinus 0.01)
      }

      test("3/4 cup") {
        val ingredient = IngredientParser.parseIngredient("1/4 cup butter\n")
        ingredient.measurement.unit shouldBe MeasurementUnit.Cup
        ingredient.measurement.quantity shouldBe (0.25 plusOrMinus 0.01)
      }

      test("1 1/2 cup") {
        val ingredient = IngredientParser.parseIngredient("1 1/2 cup butter\n")
        ingredient.measurement.unit shouldBe MeasurementUnit.Cup
        ingredient.measurement.quantity shouldBe (1.5 plusOrMinus 0.01)
      }
    }

    context("No Unit") {
      test("Decimal") {
        val ingredient = IngredientParser.parseIngredient("1 onion\n")
        ingredient.measurement.unit shouldBe MeasurementUnit.None
        ingredient.measurement.quantity shouldBe 1.0
      }

      test("Fractional") {
        val ingredient = IngredientParser.parseIngredient("1/2 onion\n")
        ingredient.measurement.unit shouldBe MeasurementUnit.None
        ingredient.measurement.quantity shouldBe 0.5
      }

      test("No Quantity") {
        val ingredient = IngredientParser.parseIngredient("salt\n")
        ingredient.measurement.unit shouldBe MeasurementUnit.None
        ingredient.measurement.quantity shouldBe 0.0
      }
    }

    context("Ingredient Name") {
      test("Single Word") {
        val ingredient = IngredientParser.parseIngredient("1 cup butter\n")
        ingredient.name shouldBe "butter"
      }

      test("Multi-words") {
        val ingredient = IngredientParser.parseIngredient("1 cup red onion, chopped\n")
        ingredient.name shouldBe "red onion"
      }
    }

    context("Comment") {
      test("Comma") {
        val ingredient = IngredientParser.parseIngredient("1 cup butter, separated\n")
        ingredient.comment shouldBe "separated"
      }

      test("Parenthesis") {
        val ingredient = IngredientParser.parseIngredient("1 cup butter (sliced)\n")
        ingredient.comment shouldBe "sliced"
      }

      test("Multi-words") {
        val ingredient = IngredientParser.parseIngredient("1 onion, thinly sliced\n")
        ingredient.comment shouldBe "thinly sliced"
      }
    }
  }
})