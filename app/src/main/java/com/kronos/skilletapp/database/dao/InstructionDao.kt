package com.kronos.skilletapp.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.kronos.skilletapp.database.entity.InstructionEntity
import com.kronos.skilletapp.database.entity.InstructionEquipmentEntity
import com.kronos.skilletapp.database.entity.InstructionIngredientEntity
import kotlinx.coroutines.flow.Flow

/**
 * Per-table DAO for the `instruction` table. It also owns the ordered selects and deletes for the two join tables
 * (`instruction_ingredient`, `instruction_equipment`) — there is no dedicated join DAO.
 */
@Dao
interface InstructionDao {

  @Upsert suspend fun upsertInstructions(instructions: List<InstructionEntity>)

  @Query("SELECT * FROM instruction WHERE recipe_id = :recipeId ORDER BY position")
  suspend fun getByRecipeId(recipeId: String): List<InstructionEntity>

  @Query("SELECT * FROM instruction WHERE recipe_id = :recipeId ORDER BY position")
  fun observeByRecipeId(recipeId: String): Flow<List<InstructionEntity>>

  @Query("DELETE FROM instruction WHERE recipe_id = :recipeId") suspend fun deleteByRecipeId(recipeId: String)

  // Join-table upserts.
  @Upsert suspend fun upsertInstructionIngredients(instructionIngredients: List<InstructionIngredientEntity>)

  @Upsert suspend fun upsertInstructionEquipment(instructionEquipment: List<InstructionEquipmentEntity>)

  // Join-table ordered selects (there is no dedicated join DAO).
  @Query("SELECT * FROM instruction_ingredient WHERE recipe_id = :recipeId ORDER BY position")
  suspend fun selectInstructionIngredients(recipeId: String): List<InstructionIngredientEntity>

  @Query("SELECT * FROM instruction_ingredient WHERE recipe_id = :recipeId ORDER BY position")
  fun observeInstructionIngredients(recipeId: String): Flow<List<InstructionIngredientEntity>>

  @Query("SELECT * FROM instruction_equipment WHERE recipe_id = :recipeId ORDER BY position")
  suspend fun selectInstructionEquipment(recipeId: String): List<InstructionEquipmentEntity>

  @Query("SELECT * FROM instruction_equipment WHERE recipe_id = :recipeId ORDER BY position")
  fun observeInstructionEquipment(recipeId: String): Flow<List<InstructionEquipmentEntity>>

  // Join-table deletes by recipe.
  @Query("DELETE FROM instruction_ingredient WHERE recipe_id = :recipeId")
  suspend fun deleteInstructionIngredientsByRecipeId(recipeId: String)

  @Query("DELETE FROM instruction_equipment WHERE recipe_id = :recipeId") suspend fun deleteInstructionEquipmentByRecipeId(recipeId: String)
}
