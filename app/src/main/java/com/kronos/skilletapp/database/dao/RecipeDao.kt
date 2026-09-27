package com.kronos.skilletapp.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.kronos.skilletapp.database.entity.RecipeEntity
import kotlinx.coroutines.flow.Flow

/** Per-table DAO for the `recipe` table. */
@Dao
interface RecipeDao {

  @Upsert suspend fun upsert(recipe: RecipeEntity)

  @Query("SELECT * FROM recipe") suspend fun getAll(): List<RecipeEntity>

  @Query("SELECT * FROM recipe") fun observeAll(): Flow<List<RecipeEntity>>

  @Query("SELECT * FROM recipe WHERE id = :id") suspend fun getById(id: String): RecipeEntity?

  @Query("SELECT * FROM recipe WHERE id = :id") fun observeById(id: String): Flow<RecipeEntity?>

  @Query("DELETE FROM recipe WHERE id = :id") suspend fun deleteById(id: String)
}
