package com.kronos.skilletapp.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.kronos.skilletapp.database.entity.EquipmentEntity
import kotlinx.coroutines.flow.Flow

/** Per-table DAO for the `equipment` table. */
@Dao
interface EquipmentDao {

  @Upsert suspend fun upsertAll(equipment: List<EquipmentEntity>)

  @Query("SELECT * FROM equipment WHERE recipe_id = :recipeId ORDER BY position")
  suspend fun getByRecipeId(recipeId: String): List<EquipmentEntity>

  @Query("SELECT * FROM equipment WHERE recipe_id = :recipeId ORDER BY position")
  fun observeByRecipeId(recipeId: String): Flow<List<EquipmentEntity>>

  @Query("DELETE FROM equipment WHERE recipe_id = :recipeId") suspend fun deleteByRecipeId(recipeId: String)
}
