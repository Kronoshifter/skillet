package com.kronos.skilletapp

import androidx.navigation.NavHostController
import kotlinx.serialization.Serializable

sealed interface Route {
  @Serializable data object RecipeList : Route
  @Serializable data class Recipe(val recipeId: String) : Route
  @Serializable data class AddEditRecipe(val title: String, val recipeId: String? = null) : Route
}

class SkilletNavigationActions(private val navController: NavHostController) {

  fun navigateToRecipe(recipeId: String) {
    navController.navigate(Route.Recipe(recipeId)) {
      popUpTo<Route.RecipeList>()
    }
  }

  fun navigateToAddEditRecipe(title: String, recipeId: String? = null) {
    navController.navigate(Route.AddEditRecipe(title, recipeId))
  }
}