package com.kronos.skilletapp.domain.scaling

import com.kronos.skilletapp.model.Ingredient

data class ScaledRecipe(
  val scaledIngredients: List<Ingredient>,
  val servings: Int,
  val factor: Float,
)
