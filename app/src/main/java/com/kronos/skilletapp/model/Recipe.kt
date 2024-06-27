package com.kronos.skilletapp.model

data class Recipe(
  val name: String,
  val description: String,
  val cover: String? = null, // cover photo
  val notes: String,
  val servings: Int,
  val time: RecipeTime,
  val source: RecipeSource,
  val ingredients: List<Ingredient>, // TODO: convert to ingredient sections
  val instructions: List<Instruction>, // TODO: convert to instruction sections
  val equipment: List<Equipment>
) {
//  val allIngredients
//    get() = ingredients.flatMap { it.ingredients }

//  val allInstructions
//    get() = instructions.flatMap { it.instructions }
}

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