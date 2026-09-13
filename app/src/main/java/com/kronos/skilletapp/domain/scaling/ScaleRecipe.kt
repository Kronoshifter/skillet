package com.kronos.skilletapp.domain.scaling

import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.Recipe
import com.kronos.measurement.model.Measurement

class ScaleRecipe {
  operator fun invoke(recipe: Recipe, targetServings: Int): ScaledRecipe {
    val factor = targetServings.toFloat() / recipe.servings
    val scaledIngredients = recipe.ingredients.map { ingredient ->
      ingredient.copy(measurement = ingredient.measurement.scale(factor))
    }
    return ScaledRecipe(scaledIngredients, targetServings, factor)
  }
}
