package com.kronos.skilletapp.model

import com.kronos.skilletapp.database.entity.RecipeEntity

data class RecipeSummary(
  val id: String,
  val name: String,
  val description: String,
  val cover: String?,
  val servings: Int,
  val prepTime: Int,
  val cookTime: Int,
  val sourceName: String,
  val sourceUrl: String,
)

fun RecipeEntity.toSummary() =
  RecipeSummary(
    id = id,
    name = name,
    description = description,
    cover = cover,
    servings = servings,
    prepTime = prepTime,
    cookTime = cookTime,
    sourceName = sourceName,
    sourceUrl = sourceUrl,
  )
