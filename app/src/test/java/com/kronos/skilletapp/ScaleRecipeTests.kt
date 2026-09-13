package com.kronos.skilletapp

import com.kronos.measurement.model.Measurement
import com.kronos.measurement.model.MeasurementUnit
import com.kronos.skilletapp.domain.scaling.ScaleRecipe
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
class ScaleRecipeTests :
  FunSpec({
    context("ScaleRecipe Tests") {
      val scaler = ScaleRecipe()

      val ingredient1 =
        Ingredient(
          name = "Flour",
          measurement = Measurement(2f, MeasurementUnit.Cup),
          raw = "2 cup Flour",
        )
      val ingredient2 =
        Ingredient(
          name = "Sugar",
          measurement = Measurement(1f, MeasurementUnit.Tablespoon),
          raw = "1 tbsp Sugar",
        )
      val ingredient3 =
        Ingredient(
          name = "Salt",
          measurement = Measurement(0.5f, MeasurementUnit.Teaspoon),
          raw = "0.5 tsp Salt",
        )

      val recipe =
        Recipe(
          id = Uuid.random().toString(),
          name = "Test Recipe",
          description = "A test recipe for scaling",
          notes = "",
          servings = 4,
          time = RecipeTime(10, 20),
          source = RecipeSource("Test", "http://test.com"),
          ingredients = listOf(ingredient1, ingredient2, ingredient3),
          instructions = listOf(Instruction("Mix ingredients")),
          equipment = emptyList(),
        )

      context("Scale up (4 -> 8, factor=2.0)") {
        test("All ingredient quantities should be doubled") {
          val result = scaler(recipe, 8)

          result.servings shouldBe 8
          result.factor shouldBe 2.0f

          result.scaledIngredients[0].measurement.quantity shouldBe 4.0f // 2 cup * 2
          result.scaledIngredients[1].measurement.quantity shouldBe 2.0f // 1 tbsp * 2
          result.scaledIngredients[2].measurement.quantity shouldBe 1.0f // 0.5 tsp * 2
        }

        test("Ingredient names and comments should be preserved") {
          val result = scaler(recipe, 8)

          result.scaledIngredients[0].name shouldBe "Flour"
          result.scaledIngredients[1].name shouldBe "Sugar"
          result.scaledIngredients[2].name shouldBe "Salt"
        }
      }

      context("Scale down (4 -> 2, factor=0.5)") {
        test("All ingredient quantities should be halved") {
          val result = scaler(recipe, 2)

          result.servings shouldBe 2
          result.factor shouldBe 0.5f

          result.scaledIngredients[0].measurement.quantity shouldBe 1.0f // 2 cup * 0.5
          result.scaledIngredients[1].measurement.quantity shouldBe 0.5f // 1 tbsp * 0.5
          result.scaledIngredients[2].measurement.quantity shouldBe 0.25f // 0.5 tsp * 0.5
        }
      }

      context("Identity (4 -> 4, factor=1.0)") {
        test("All ingredient quantities should remain unchanged") {
          val result = scaler(recipe, 4)

          result.servings shouldBe 4
          result.factor shouldBe 1.0f

          result.scaledIngredients[0].measurement.quantity shouldBe 2.0f
          result.scaledIngredients[1].measurement.quantity shouldBe 1.0f
          result.scaledIngredients[2].measurement.quantity shouldBe 0.5f
        }
      }
    }
  })
