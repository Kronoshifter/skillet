package com.kronos.skilletapp.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Upsert
import com.kronos.skilletapp.model.Recipe
import kotlinx.coroutines.flow.Flow

@Database(entities = [Recipe::class], version = 1)
abstract class RecipeDatabase : RoomDatabase() {
  abstract fun recipeDao(): RecipeDao
}

@Dao
interface RecipeDao {

  @Query("SELECT * FROM recipe")
  fun getAll(): Flow<List<Recipe>>

  @Query("SELECT * FROM recipe WHERE id = :id")
  fun getById(id: String): Flow<Recipe>

  @Query("SELECT * FROM recipe WHERE name LIKE :name LIMIT 1")
  fun getByName(name: String): Recipe

  @Upsert(entity = Recipe::class)
  suspend fun upsert(recipe: Recipe)

  @Delete
  suspend fun delete(recipe: Recipe)
}