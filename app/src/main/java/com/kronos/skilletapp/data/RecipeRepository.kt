package com.kronos.skilletapp.data

import com.kronos.skilletapp.model.Equipment
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.Instruction
import com.kronos.skilletapp.model.Recipe
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
  suspend fun fetchRecipe(id: String): Recipe
  fun observeRecipe(id: String): Flow<Recipe>
  suspend fun fetchRecipes(): List<Recipe>
  fun observeRecipes(): Flow<List<Recipe>>
  suspend fun upsert(recipe: Recipe): Unit
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
  ): String
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
  )
}
