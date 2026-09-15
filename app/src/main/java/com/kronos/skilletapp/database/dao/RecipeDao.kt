package com.kronos.skilletapp.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.kronos.skilletapp.database.entity.RecipeEntity
import kotlinx.coroutines.flow.Flow

/**
 * Per-table DAO for the `recipe` table. Not yet registered in `@Database` or Koin (that is a later
 * gate); it compiles as an orphan `@Dao` interface in this additive gate.
 */
@Dao
interface RecipeDao {

  @Upsert suspend fun upsert(recipe: RecipeEntity)

  @Query("SELECT * FROM recipe WHERE id = :id") suspend fun getById(id: String): RecipeEntity?

  @Query("SELECT * FROM recipe WHERE id = :id") fun observeById(id: String): Flow<RecipeEntity?>

  @Query("DELETE FROM recipe WHERE id = :id") suspend fun deleteById(id: String)
}
