package com.kronos.skilletapp.domain.scraping

data class ScrapedRecipe(
  val name: String,
  val description: String? = null,
  val servings: Int? = null,
  val prepTimeMinutes: Int? = null,
  val cookTimeMinutes: Int? = null,
  val sourceUrl: String,
  val sourceName: String? = null,
  val ingredients: List<String> = emptyList(),
  val instructions: List<String> = emptyList(),
)
