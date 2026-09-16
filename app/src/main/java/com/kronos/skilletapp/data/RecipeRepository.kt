package com.kronos.skilletapp.data

import com.kronos.skilletapp.model.Recipe
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
  suspend fun fetchRecipe(id: String): Recipe
  fun observeRecipe(id: String): Flow<Recipe>
  suspend fun fetchRecipes(): List<Recipe>
  fun observeRecipes(): Flow<List<Recipe>>
  suspend fun upsert(recipe: Recipe): Unit
  suspend fun createRecipe(recipe: Recipe): String
  suspend fun updateRecipe(id: String, recipe: Recipe)
}
