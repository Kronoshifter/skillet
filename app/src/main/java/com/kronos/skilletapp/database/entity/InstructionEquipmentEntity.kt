package com.kronos.skilletapp.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
  tableName = "instruction_equipment",
  primaryKeys = ["instruction_id", "equipment_id"],
  foreignKeys = [
    ForeignKey(
      entity = InstructionEntity::class,
      parentColumns = ["id"],
      childColumns = ["instruction_id"],
      onDelete = ForeignKey.CASCADE,
    ),
    ForeignKey(
      entity = EquipmentEntity::class,
      parentColumns = ["id"],
      childColumns = ["equipment_id"],
      onDelete = ForeignKey.CASCADE,
    ),
  ],
)
data class InstructionEquipmentEntity(
  @ColumnInfo(name = "instruction_id") val instructionId: String,
  @ColumnInfo(name = "equipment_id") val equipmentId: String,
  @ColumnInfo(name = "recipe_id") val recipeId: String,
  val position: Int,
)
