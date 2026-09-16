package com.kronos.skilletapp.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.kronos.measurement.model.Measurement

@Entity(
  tableName = "ingredient",
  foreignKeys = [
    ForeignKey(
      entity = RecipeEntity::class,
      parentColumns = ["id"],
      childColumns = ["recipe_id"],
      onDelete = ForeignKey.CASCADE,
    ),
  ],
)
data class IngredientEntity(
  @PrimaryKey val id: String,
  @ColumnInfo(name = "recipe_id") val recipeId: String,
  val position: Int,
  val name: String,
  val measurement: Measurement,
  val raw: String,
  val comment: String? = null,
)
