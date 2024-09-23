package com.kronos.skilletapp

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.kronos.skilletapp.ui.screen.AddEditRecipeScreen
import com.kronos.skilletapp.ui.screen.cooking.CookingScreen
import com.kronos.skilletapp.ui.screen.recipelist.RecipeListScreen
import com.kronos.skilletapp.ui.screen.recipe.RecipeScreen

@Composable
fun SkilletNavGraph(
  modifier: Modifier = Modifier,
  navController: NavHostController = rememberNavController(),
  startDestination: Route = Route.RecipeList,
  navActions: SkilletNavigationActions = remember(navController) { SkilletNavigationActions(navController) },
) {
  NavHost(
    navController = navController,
    startDestination = startDestination,
    popExitTransition = {
      slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End) + fadeOut()
    },
  ) {
    composable<Route.RecipeList> {
      RecipeListScreen(
        onAddRecipe = { navActions.navigateToAddEditRecipe("Add Recipe") },
        onRecipeClick = { navActions.navigateToRecipe(it) }
      )
    }
    composable<Route.Recipe>(
      enterTransition = {
        scaleIn() + fadeIn()
      },
      exitTransition = {
        scaleOut() + fadeOut()
      }
    ) {
      val args = it.toRoute<Route.Recipe>()
      RecipeScreen(
        onBack = { navController.popBackStack() },
        onEdit = { navActions.navigateToAddEditRecipe("Edit Recipe", args.recipeId) },
        onCook = { selectedUnits -> navActions.navigateToCooking(args.recipeId, selectedUnits) }
      )
    }
    composable<Route.AddEditRecipe> { backStackEntry ->
      val args = backStackEntry.toRoute<Route.AddEditRecipe>()
      AddEditRecipeScreen(
        title = args.title,
        onBack = { navController.popBackStack() },
        onRecipeUpdate = { recipeId ->
          navActions.navigateToRecipe(recipeId)
        },
      )
    }
    composable<Route.Cooking> { backStackEntry ->
      val args = backStackEntry.toRoute<Route.Cooking>()
      CookingScreen(
        onBack = { navController.popBackStack() },
      )
    }
  }
}