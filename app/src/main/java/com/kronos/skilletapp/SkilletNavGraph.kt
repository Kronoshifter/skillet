package com.kronos.skilletapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.kronos.skilletapp.ui.RecipeListScreen
import com.kronos.skilletapp.ui.RecipeScreen

@Composable
fun SkilletNavGraph(
  modifier: Modifier = Modifier,
  navController: NavHostController = rememberNavController(),
  startDestination: Route = Route.RecipeList,
  navActions: SkilletNavigationActions = remember(navController) { SkilletNavigationActions(navController) },
) {
  NavHost(navController = navController, startDestination = startDestination) {
    composable<Route.RecipeList> {
      RecipeListScreen(
        onRecipeClick = { navActions.navigateToRecipe("test") }
      )
    }
    composable<Route.Recipe> { backStackEntry ->
      val route = backStackEntry.toRoute<Route.Recipe>()
      RecipeScreen(
        id = route.recipeId,
        onBack = { navController.popBackStack() }
      )
    }
    composable<Route.AddEditRecipe> { backStackEntry ->
      val recipeId = backStackEntry.toRoute<Route.AddEditRecipe>().recipeId
//      AddEditRecipeScreen(recipeId = recipeId)
    }
  }
}