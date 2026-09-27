package com.kronos.skilletapp.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
  tableName = "recipe",
  indices = [Index(value = ["name"])],
)
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
