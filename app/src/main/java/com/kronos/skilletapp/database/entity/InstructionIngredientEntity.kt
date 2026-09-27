package com.kronos.skilletapp.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index

@Entity(
  tableName = "instruction_ingredient",
  primaryKeys = ["instruction_id", "ingredient_id"],
  indices = [Index(value = ["instruction_id"]), Index(value = ["ingredient_id"])],
  foreignKeys =
    [
      ForeignKey(
        entity = InstructionEntity::class,
        parentColumns = ["id"],
        childColumns = ["instruction_id"],
        onDelete = ForeignKey.CASCADE,
      ),
      ForeignKey(
        entity = IngredientEntity::class,
        parentColumns = ["id"],
        childColumns = ["ingredient_id"],
        onDelete = ForeignKey.CASCADE,
      ),
    ],
)
data class InstructionIngredientEntity(
  @ColumnInfo(name = "instruction_id") val instructionId: String,
  @ColumnInfo(name = "ingredient_id") val ingredientId: String,
  @ColumnInfo(name = "recipe_id") val recipeId: String,
  val position: Int,
)
