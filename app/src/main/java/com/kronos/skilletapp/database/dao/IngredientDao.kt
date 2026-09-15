package com.kronos.skilletapp.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.kronos.skilletapp.database.entity.IngredientEntity
import kotlinx.coroutines.flow.Flow

/**
 * Per-table DAO for the `ingredient` table. Not yet registered in `@Database` or Koin (that is a
 * later gate); it compiles as an orphan `@Dao` interface in this additive gate.
 */
@Dao
interface IngredientDao {

  @Upsert suspend fun upsert(ingredient: IngredientEntity)

  @Query("SELECT * FROM ingredient WHERE recipe_id = :recipeId ORDER BY position")
  suspend fun getByRecipeId(recipeId: String): List<IngredientEntity>

  @Query("SELECT * FROM ingredient WHERE recipe_id = :recipeId ORDER BY position")
  fun observeByRecipeId(recipeId: String): Flow<List<IngredientEntity>>

  @Query("DELETE FROM ingredient WHERE recipe_id = :recipeId")
  suspend fun deleteByRecipeId(recipeId: String)
}
