package com.kronos.skilletapp.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.kronos.skilletapp.database.entity.EquipmentEntity
import kotlinx.coroutines.flow.Flow

/**
 * Per-table DAO for the `equipment` table. Not yet registered in `@Database` or Koin (that is a
 * later gate); it compiles as an orphan `@Dao` interface in this additive gate.
 */
@Dao
interface EquipmentDao {

  @Upsert suspend fun upsert(equipment: EquipmentEntity)

  @Query("SELECT * FROM equipment WHERE recipe_id = :recipeId ORDER BY position")
  suspend fun getByRecipeId(recipeId: String): List<EquipmentEntity>

  @Query("SELECT * FROM equipment WHERE recipe_id = :recipeId ORDER BY position")
  fun observeByRecipeId(recipeId: String): Flow<List<EquipmentEntity>>

  @Query("DELETE FROM equipment WHERE recipe_id = :recipeId")
  suspend fun deleteByRecipeId(recipeId: String)
}
