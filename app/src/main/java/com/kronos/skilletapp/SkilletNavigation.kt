package com.kronos.skilletapp

import android.R.id.selectedIcon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.google.android.material.bottomnavigation.BottomNavigationView
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

sealed class BottomNavItems<T : Route>(
  val label: String,
  val route: T,
  private val icon: ImageVector,
  private val selectedIcon: ImageVector,
) {
  fun icon(isSelected: Boolean): ImageVector = if (isSelected) {
    selectedIcon
  } else {
    icon
  }

  data object RecipeList : BottomNavItems<Route.RecipeList>(
    label = "Recipes",
    route = Route.RecipeList(),
    icon = Icons.AutoMirrored.Outlined.ListAlt,
    selectedIcon = Icons.AutoMirrored.Filled.ListAlt
  )

  data object Collections : BottomNavItems<Route.AddEditRecipe>(
    label = "Collections",
    route = Route.AddEditRecipe("Add Recipe"),
    icon = Icons.Outlined.Dashboard,
    selectedIcon = Icons.Filled.Dashboard
  )

  companion object {
    val values by lazy {
      listOf(
        RecipeList,
        Collections
      )
    }
  }
}