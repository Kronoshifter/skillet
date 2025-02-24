package com.kronos.skilletapp

import androidx.navigation.NavHostController
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.MeasurementUnit
import kotlinx.serialization.Serializable

sealed interface Route {
  @Serializable data class RecipeList(val sharedUrl: String? = null) : Route
  @Serializable data class Recipe(val recipeId: String) : Route
  @Serializable data class AddEditRecipe(val title: String, val recipeId: String? = null, val url: String? = null) : Route

  @Serializable data class Cooking(val recipeId: String, val scale: Float) : Route
}

class SkilletNavigationActions(private val navController: NavHostController) {

  fun navigateToRecipeList() {
    navController.navigate(Route.RecipeList()) {
      restoreState = false
      launchSingleTop = true

      popUpTo<Route.RecipeList> {
        inclusive = true
        saveState = false
      }
    }
  }

  fun navigateToRecipe(recipeId: String) {
    navController.navigate(Route.Recipe(recipeId)) {
      popUpTo<Route.RecipeList>()
    }
  }

  fun navigateToAddEditRecipe(title: String, recipeId: String? = null, url: String? = null) {
    navController.navigate(Route.AddEditRecipe(title, recipeId, url))
  }

  fun navigateToCooking(recipeId: String, scale: Float) {
    navController.navigate(Route.Cooking(recipeId, scale))
  }
}