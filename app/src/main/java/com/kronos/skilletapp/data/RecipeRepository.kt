package com.kronos.skilletapp.data

import com.kronos.skilletapp.database.RecipeDao
import com.kronos.skilletapp.model.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class RecipeRepository(private val database: RecipeDao) {
  suspend fun fetchRecipe(id: String) = database.getById(id)

  fun observeRecipe(id: String) = database.observeById(id)

  suspend fun fetchRecipes() = database.getAll()

  fun observeRecipes() = database.observeAll()

  suspend fun upsert(recipe: Recipe) = database.upsert(recipe)

  @OptIn(ExperimentalUuidApi::class)
  suspend fun createRecipe(
    name: String,
    description: String,
    notes: String,
    servings: Int,
    prepTime: Int,
    cookTime: Int,
    source: String,
    sourceName: String,
    image: String?,
    ingredients: List<Ingredient>,
    instructions: List<Instruction>,
    equipment: List<Equipment>,
  ): String {
    val recipe = Recipe(
      id = Uuid.random().toString(),
      name = name,
      description = description,
      cover = image,
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
    image: String?,
    ingredients: List<Ingredient>,
    instructions: List<Instruction>,
    equipment: List<Equipment>,
  ) {
    val recipe = fetchRecipe(id).copy(
      name = name,
      description = description,
      cover = image,
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

//  init {
//    runBlocking {
//      initFakeRecipes()
//    }
//  }

  private suspend fun initFakeRecipes() {
    val ingredients = listOf(
      Ingredient("Mini Shells Pasta", measurement = Measurement(8f, MeasurementUnit.Ounce), "8 oz Mini Shells Pasta"),
      Ingredient("Olive Oil", measurement = Measurement(1f, MeasurementUnit.Tablespoon), "1 tbsp Olive Oil"),
      Ingredient("Butter", measurement = Measurement(1f, MeasurementUnit.Tablespoon), "1 tbsp Butter"),
      Ingredient(
        name = "Garlic",
        measurement = Measurement(2f, MeasurementUnit.Custom("clove")),
        raw = "2 cloves Garlic"
      ),
      Ingredient("Flour", measurement = Measurement(2f, MeasurementUnit.Tablespoon), raw = "2 tbsp Flour"),
      Ingredient("Chicken Broth", measurement = Measurement(0.75f, MeasurementUnit.Cup), raw = "3/4 cup chicken broth"),
      Ingredient("Milk", measurement = Measurement(2.5f, MeasurementUnit.Cup), raw = "2 1/2 cups milk", comment = "separated"),
      Ingredient("Salt", measurement = Measurement(0f, MeasurementUnit.None), raw = "Salt, to taste", comment = "to taste"),
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
      upsert(recipe.copy(id = "recipe-$it", name = "Recipe $it"))
    }
  }
}