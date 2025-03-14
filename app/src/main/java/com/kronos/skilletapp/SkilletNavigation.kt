package com.kronos.skilletapp

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import kotlinx.serialization.Serializable

@Serializable
data class SharedRecipe(val url: String, val id: String)

sealed interface Route {
  @Serializable data class RecipeList(val sharedRecipe: SharedRecipe? = null) : Route
  @Serializable data class Recipe(val recipeId: String) : Route
  @Serializable data class AddEditRecipe(val title: String, val recipeId: String? = null, val url: String? = null) : Route
  @Serializable data class Cooking(val recipeId: String, val scale: Float) : Route
}

class SkilletNavigationActions(private val navController: NavHostController) {

  fun navigateToRecipeList() {
    navController.navigate(Route.RecipeList())
  }

  fun navigateToRecipe(recipeId: String) {
    navController.navigate(Route.Recipe(recipeId)) {
      popUpTo<Route.RecipeList>()
    }
  }

  fun navigateToAddEditRecipe(title: String, recipeId: String? = null, url: String? = null) {
    navController.navigate(Route.AddEditRecipe(title, recipeId, url)) {
      restoreState = true
    }
  }

  fun navigateToCooking(recipeId: String, scale: Float) {
    navController.navigate(Route.Cooking(recipeId, scale)) {
      restoreState = true
    }
  }

  fun navigateViaBottomNav(route: Route) {
    navController.navigate(route) {
      launchSingleTop = true
      restoreState = true

      popUpTo(navController.graph.findStartDestination().id) {
        saveState = true
      }
    }
  }
}

val LocalNavigationActions = compositionLocalOf<SkilletNavigationActions> { error("No SkilletNavigationActions provided") }
val LocalNavController = compositionLocalOf<NavHostController> { error("No NavController provided") }