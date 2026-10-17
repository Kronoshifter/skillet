package com.kronos.skilletapp

import com.kronos.measurement.model.Measurement
import com.kronos.measurement.model.MeasurementUnit
import com.kronos.skilletapp.domain.validation.ValidateRecipe
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.Instruction
import com.kronos.skilletapp.model.Recipe
import com.kronos.skilletapp.model.RecipeSource
import com.kronos.skilletapp.model.RecipeTime
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ValidateRecipeTests :
  FunSpec({
    context("ValidateRecipe Tests") {
      val validator = ValidateRecipe()

      val ingredient = Ingredient("Pasta", Measurement(1f, MeasurementUnit.Cup), "1 cup Pasta")
      val instruction = Instruction("Boil water")
      val recipe =
        Recipe(
          id = Uuid.random().toString(),
          name = "Test Recipe",
          description = "A test",
          notes = "Test notes",
          servings = 4,
          time = RecipeTime(5, 10),
          source = RecipeSource("Test", "http://test.com"),
          ingredients = listOf(ingredient),
          instructions = listOf(instruction),
          equipment = emptyList(),
        )

      context("Validation failure scenarios") {
        test("Blank name should return error") {
          val result = validator("", listOf(ingredient), listOf(instruction), 4, 10)
          result.isErr shouldBe true
        }

        test("No ingredients should return error") {
          val result = validator("Recipe", emptyList(), listOf(instruction), 4, 10)
          result.isErr shouldBe true
        }

        test("No instructions should return error") {
          val result = validator("Recipe", listOf(ingredient), emptyList(), 4, 10)
          result.isErr shouldBe true
        }

        test("Servings <= 0 should return error") {
          val result = validator("Recipe", listOf(ingredient), listOf(instruction), 0, 10)
          result.isErr shouldBe true
        }

        test("Cook time <= 0 should return error") {
          val result = validator("Recipe", listOf(ingredient), listOf(instruction), 4, 0)
          result.isErr shouldBe true
        }
      }

      context("Success scenario") {
        test("Valid recipe should return ok") {
          val result = validator("Recipe", listOf(ingredient), listOf(instruction), 4, 10)
          result.isOk shouldBe true
        }
      }
    }
  })
