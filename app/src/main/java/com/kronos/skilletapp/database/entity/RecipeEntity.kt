package com.kronos.skilletapp.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipe")
data class RecipeEntity(
  @PrimaryKey val id: String,
  val name: String,
  val description: String,
  val cover: String? = null,
  val notes: String,
  val servings: Int,
  @ColumnInfo(name = "prep_time") val prepTime: Int,
  @ColumnInfo(name = "cook_time") val cookTime: Int,
  @ColumnInfo(name = "source_name") val sourceName: String,
  @ColumnInfo(name = "source_url") val sourceUrl: String,
)
