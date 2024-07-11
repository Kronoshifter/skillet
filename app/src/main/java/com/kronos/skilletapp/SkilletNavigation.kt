package com.kronos.skilletapp

import androidx.navigation.NavHostController
import kotlinx.serialization.Serializable

@Serializable
sealed class Routes {
  @Serializable data object RecipeList : Routes()
  @Serializable data class Recipe(val recipeId: String) : Routes()
  @Serializable data class AddEditRecipe(val recipeId: String?) : Routes()
}

class SkilletNavigationActions(private val navController: NavHostController) {

  fun navigateToRecipe(recipeId: String) {
    navController.navigate(Routes.Recipe(recipeId))
  }

  fun navigateToAddEditRecipe(recipeId: String?) {
    navController.navigate(Routes.AddEditRecipe(recipeId)) {
      popUpTo(navController.graph.startDestinationId) {
        inclusive = true
        saveState = true
      }
      launchSingleTop = true
      restoreState = true
    }
  }
}