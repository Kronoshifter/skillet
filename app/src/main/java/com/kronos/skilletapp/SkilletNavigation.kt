package com.kronos.skilletapp

import androidx.navigation.NavHostController
import kotlinx.serialization.Serializable

@Serializable
sealed class Route {
  @Serializable data object RecipeList : Route()
  @Serializable data class Recipe(val recipeId: String) : Route()
  @Serializable data class AddEditRecipe(val recipeId: String?) : Route()
}

class SkilletNavigationActions(private val navController: NavHostController) {

  fun navigateToRecipe(recipeId: String) {
    navController.navigate(Route.Recipe(recipeId))
  }

  fun navigateToAddEditRecipe(recipeId: String?) {
    navController.navigate(Route.AddEditRecipe(recipeId)) {
      popUpTo(navController.graph.startDestinationId) {
        inclusive = true
        saveState = true
      }
      launchSingleTop = true
      restoreState = true
    }
  }
}