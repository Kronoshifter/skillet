package com.kronos.skilletapp.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.kronos.skilletapp.database.entity.IngredientEntity
import kotlinx.coroutines.flow.Flow

/** Per-table DAO for the `ingredient` table. */
@Dao
interface IngredientDao {

  @Upsert suspend fun upsertAll(ingredients: List<IngredientEntity>)

  @Query("SELECT * FROM ingredient WHERE recipe_id = :recipeId ORDER BY position")
  suspend fun getByRecipeId(recipeId: String): List<IngredientEntity>

  @Query("SELECT * FROM ingredient WHERE recipe_id = :recipeId ORDER BY position")
  fun observeByRecipeId(recipeId: String): Flow<List<IngredientEntity>>

  @Query("DELETE FROM ingredient WHERE recipe_id = :recipeId") suspend fun deleteByRecipeId(recipeId: String)
}
