package com.kronos.skilletapp

import com.kronos.measurement.model.Measurement
import com.kronos.measurement.model.MeasurementUnit
import com.kronos.skilletapp.data.RecipeMapper
import com.kronos.skilletapp.model.Equipment
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.Instruction
import com.kronos.skilletapp.model.Recipe
import com.kronos.skilletapp.model.RecipeSource
import com.kronos.skilletapp.model.RecipeTime
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class RecipeMapperTests :
  FunSpec({
    context("RecipeMapper Tests") {
      val mapper = RecipeMapper()

      // Explicit ids so we can assert dedup/ordering precisely.
      val flour = Ingredient("Flour", Measurement(2f, MeasurementUnit.Cup), "2 cup Flour", id = "F")
      val sugar = Ingredient("Sugar", Measurement(1f, MeasurementUnit.Tablespoon), "1 tbsp Sugar", id = "S")
      val bowl = Equipment("Mixing Bowl", id = "B")

      // `flour` is shared: referenced at the recipe level AND inside an instruction.
      val mixDry = Instruction("Mix dry ingredients", ingredients = listOf(flour), id = "I0")
      val addWet = Instruction("Add wet ingredients", ingredients = listOf(sugar), equipment = listOf(bowl), id = "I1")

      val recipe =
        Recipe(
          id = "R",
          name = "Test Recipe",
          description = "A test",
          cover = null,
          notes = "Notes",
          servings = 4,
          time = RecipeTime(10, 20),
          source = RecipeSource("Src", "http://x"),
          ingredients = listOf(flour, sugar),
          instructions = listOf(mixDry, addWet),
          equipment = listOf(bowl),
        )

      val mapped = mapper.toEntities(recipe)

      context("Domain -> entities") {
        test("Recipe row flattens time and source into scalar columns") {
          mapped.recipe.id shouldBe "R"
          mapped.recipe.prepTime shouldBe 10
          mapped.recipe.cookTime shouldBe 20
          mapped.recipe.sourceName shouldBe "Src"
          mapped.recipe.sourceUrl shouldBe "http://x"
        }

        test("Shared ingredient produces exactly one IngredientEntity row") {
          mapped.ingredients.count { it.id == "F" } shouldBe 1
          mapped.ingredients.size shouldBe 2
        }

        test("Child rows are keyed by id and ordered by position") {
          mapped.ingredients.map { it.id to it.position } shouldBe listOf("F" to 0, "S" to 1)
          mapped.instructions.map { it.id to it.position } shouldBe listOf("I0" to 0, "I1" to 1)
          mapped.equipment.map { it.id to it.position } shouldBe listOf("B" to 0)
        }

        test("Join rows reference ids and carry per-instruction position") {
          val iing = mapped.instructionIngredients.map { it.instructionId to it.ingredientId to it.position }
          val iequip = mapped.instructionEquipment.map { it.instructionId to it.equipmentId to it.position }

          iing shouldBe listOf("I0" to "F" to 0, "I1" to "S" to 0)
          iequip shouldBe listOf("I1" to "B" to 0)
        }

        test("Every child and join row carries the recipe id") {
          mapped.ingredients.all { it.recipeId == "R" } shouldBe true
          mapped.instructions.all { it.recipeId == "R" } shouldBe true
          mapped.equipment.all { it.recipeId == "R" } shouldBe true
          mapped.instructionIngredients.all { it.recipeId == "R" } shouldBe true
          mapped.instructionEquipment.all { it.recipeId == "R" } shouldBe true
        }
      }

      context("Entities -> domain round-trip") {
        val restored = mapper.toDomain(mapped)

        test("Recipe scalar fields and VOs are preserved") {
          restored.id shouldBe "R"
          restored.name shouldBe "Test Recipe"
          restored.description shouldBe "A test"
          restored.cover shouldBe null
          restored.notes shouldBe "Notes"
          restored.servings shouldBe 4
          restored.time shouldBe RecipeTime(10, 20)
          restored.source shouldBe RecipeSource("Src", "http://x")
        }

        test("Ingredient and equipment lists preserve fields and order") {
          restored.ingredients shouldBe listOf(flour, sugar)
          restored.equipment shouldBe listOf(bowl)
        }

        test("Instructions reattach shared ingredients/equipment in order") {
          restored.instructions.map { it.id } shouldBe listOf("I0", "I1")
          restored.instructions[0].ingredients shouldBe listOf(flour)
          restored.instructions[1].ingredients shouldBe listOf(sugar)
          restored.instructions[1].equipment shouldBe listOf(bowl)
        }
      }
    }
  })
