package com.kronos.skilletapp

import androidx.navigation.NavHostController
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.MeasurementUnit
import kotlinx.serialization.Serializable

sealed interface Route {
  @Serializable data object RecipeList : Route
  @Serializable data class Recipe(val recipeId: String) : Route
  @Serializable data class AddEditRecipe(val title: String, val recipeId: String? = null) : Route

  //TODO: create custom NavType for selectedUnits
  @Serializable data class Cooking(val recipeId: String, /*val selectedUnits: Map<Ingredient, MeasurementUnit?>*/) : Route
}

class SkilletNavigationActions(private val navController: NavHostController) {

  fun navigateToRecipeList() {
    navController.navigate(Route.RecipeList) {
      popUpTo<Route.RecipeList> {
        inclusive = true
      }
    }
  }

  fun navigateToRecipe(recipeId: String) {
    navController.navigate(Route.Recipe(recipeId)) {
      popUpTo<Route.RecipeList>()
    }
  }

  fun navigateToAddEditRecipe(title: String, recipeId: String? = null) {
    navController.navigate(Route.AddEditRecipe(title, recipeId))
  }

  fun navigateToCooking(recipeId: String, selectedUnits: Map<Ingredient, MeasurementUnit?>) {
    navController.navigate(Route.Cooking(recipeId, /*selectedUnits*/))
  }
}