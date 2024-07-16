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
        onAddRecipe = { navActions.navigateToAddEditRecipe() },
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
    ) { backStackEntry ->
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