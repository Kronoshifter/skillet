package com.kronos.skilletapp.data

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.expect
import com.github.michaelbull.result.toResultOr
import com.kronos.skilletapp.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*
import kotlin.time.Duration.Companion.seconds

class RecipeRepository {
  private val recipeSource = mutableMapOf<String, Recipe>()

  suspend fun fetchRecipe(id: String): Result<Recipe, SkilletError> {
    delay(1.seconds)
    return recipeSource[id].toResultOr { SkilletError("No recipe with id: $id") }
  }

  fun fetchRecipeStream(id: String): Flow<Result<Recipe, SkilletError>> = flow {
    emit(fetchRecipe(id))
  }

  suspend fun fetchRecipes(): List<Recipe> {
    delay(1.seconds)
    return recipeSource.values.toList()
  }

  fun fetchRecipesStream(): Flow<List<Recipe>> = flow { emit(fetchRecipes()) }

  fun upsert(recipe: Recipe) {
    recipeSource[recipe.id] = recipe
  }

  suspend fun createRecipe(
    name: String,
    description: String,
    notes: String,
    servings: Int,
    prepTime: Int,
    cookTime: Int,
    source: String,
    sourceName: String,
    ingredients: List<Ingredient>,
    instructions: List<Instruction>,
    equipment: List<Equipment>,
  ): String {
    val recipe = Recipe(
      id = UUID.randomUUID().toString(),
      name = name,
      description = description,
      notes = notes,
      servings = servings,
      time = RecipeTime(prepTime, cookTime),
      source = RecipeSource(sourceName, source),
      ingredients = ingredients,
      instructions = instructions,
      equipment = equipment
    )

    upsert(recipe)

    return recipe.id
  }

  suspend fun updateRecipe(
    id: String,
    name: String,
    description: String,
    notes: String,
    servings: Int,
    prepTime: Int,
    cookTime: Int,
    source: String,
    sourceName: String,
    ingredients: List<Ingredient>,
    instructions: List<Instruction>,
    equipment: List<Equipment>,
  ) {
    val recipe = fetchRecipe(id).expect { "No recipe with id: $id" }.copy(
      name = name,
      description = description,
      notes = notes,
      servings = servings,
      time = RecipeTime(prepTime, cookTime),
      source = RecipeSource(sourceName, source),
      ingredients = ingredients,
      instructions = instructions,
      equipment = equipment
    )

    upsert(recipe)
  }

  suspend fun refreshRecipes() {
    recipeSource.clear()
    delay(1.seconds)
    initFakeRecipes()
  }

  init {
    initFakeRecipes()
  }

  private fun initFakeRecipes() {
    val ingredients = listOf(
      Ingredient("Mini Shells Pasta", measurement = Measurement(8.0, MeasurementUnit.Ounce)),
      Ingredient("Olive Oil", measurement = Measurement(1.0, MeasurementUnit.Tablespoon)),
      Ingredient("Butter", measurement = Measurement(1.0, MeasurementUnit.Tablespoon)),
      Ingredient(
        "Garlic",
        measurement = Measurement(2.0, MeasurementUnit.Custom("clove"))
      ),
      Ingredient("Flour", measurement = Measurement(2.0, MeasurementUnit.Tablespoon)),
      Ingredient("Chicken Broth", measurement = Measurement(0.75, MeasurementUnit.Cup)),
      Ingredient("Milk", measurement = Measurement(2.5, MeasurementUnit.Cup), comment = "separated"),
      Ingredient("Salt", measurement = Measurement(0.0, MeasurementUnit.None)),
    )

    val instructions = listOf(
      Instruction(
        text = "Cook pasta in a pot of salted boiling water until al dente",
        ingredients = ingredients.take(1)
      ),
      Instruction(
        text = "Return pot to stove over medium heat then ass butter and olive oil. Once melted, add garlic then saute until light golden brown, about 30 seconds, being very careful not to burn. Sprinkle in flour then whisk and saute for 1 minute. Slowly pour in chicken broth and milk while whisking until mixture is smooth. Season with salt and pepper then switch to a wooden spoon and stir constantly until mixture is thick and bubbly, 4.-5 minutes.",
        ingredients = ingredients.slice(1..6)
      ),
      Instruction(
        text = "Remove pot from heat then stir in parmesan cheese, garlic powder, and parsley flakes until smooth. Add cooked pasta then stir to combine. Taste then adjust salt and pepper if necessary, and then serve.",
        ingredients = emptyList()
      ),
    )

    val recipe = Recipe(
      id = "test",
      name = "Creamy Garlic Pasta Shells",
      ingredients = ingredients,
      instructions = instructions,
      equipment = emptyList(),
      servings = 4,
      description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
      time = RecipeTime(15, 15),
      source = RecipeSource("My Brain", "My Brain"),
      notes = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
    )

    upsert(recipe)

    repeat(10) {
      upsert(
        Recipe(
          id = "recipe-$it",
          name = "Recipe $it",
          ingredients = ingredients,
          instructions = instructions,
          equipment = emptyList(),
          servings = 4,
          description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
          time = RecipeTime(15, 15),
          source = RecipeSource("My Brain", "My Brain"),
          notes = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
        )
      )
    }
  }
}