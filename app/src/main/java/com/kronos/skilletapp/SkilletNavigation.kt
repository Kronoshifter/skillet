package com.kronos.skilletapp

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.ui.graphics.vector.ImageVector
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

  fun navigateViaBottomNav(route: Route) {
    navController.navigate(route) {
      launchSingleTop = true
      restoreState = true

      popUpTo(route = route) {
        saveState = true
      }
    }

//    navController.popBackStack(route = route, inclusive = false)
  }
}

sealed class BottomNavItems<T : Route>(
  val label: String,
  val icon: ImageVector,
  val route: T
) {
  data object RecipeList : BottomNavItems<Route.RecipeList>(label = "Recipes", icon = Icons.AutoMirrored.Filled.List, route = Route.RecipeList())

  companion object {
    val values by lazy {
      listOf(
        RecipeList
      )
    }
  }
}