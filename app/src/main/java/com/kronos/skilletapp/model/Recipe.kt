package com.kronos.skilletapp.model

data class Recipe(
  val name: String,
  val description: String,
  val cover: String, // cover photo
  val notes: String,
  val servings: Int,
  val time: RecipeTime,
  val source: RecipeSource,
  val ingredients: List<Ingredient>,
//  val instructions: List<Instruction>,
//  val equipment: List<Equipment>
)

data class RecipeTime(
  val preparation: Int,
  val cooking: Int,
) {

  val total: Int
    get() = preparation + cooking
}

data class RecipeSource(
  val name: String,
  val source: String
)