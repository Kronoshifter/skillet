package com.kronos.skilletapp.scraping

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecipeHtml(
  val name: String,
  val description: String = "",
  @SerialName("recipeIngredient") val ingredients: List<String>,
  @SerialName("recipeInstructions") val instructions: List<InstructionHtml>,
  val prepTime: String,
  val cookTime: String,
  @Serializable(with = ListOrStringSerializer::class) val recipeYield: String,
)

@Serializable
data class InstructionHtml(
  val text: String,
)

class RecipeScraper {
//  fun extractRecipe(url: String): Recipe {
//
//  }
}