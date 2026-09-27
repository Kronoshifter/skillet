package com.kronos.skilletapp.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
  tableName = "equipment",
  indices = [Index(value = ["recipe_id"])],
  foreignKeys =
    [
      ForeignKey(
        entity = RecipeEntity::class,
        parentColumns = ["id"],
        childColumns = ["recipe_id"],
        onDelete = ForeignKey.CASCADE,
      )
    ],
)
data class EquipmentEntity(
  @PrimaryKey val id: String,
  @ColumnInfo(name = "recipe_id") val recipeId: String,
  val position: Int,
  val name: String,
)
