package com.kronos.skilletapp

import android.R.attr.mimeType
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.kronos.skilletapp.ui.screen.AddEditRecipeScreen
import com.kronos.skilletapp.ui.screen.cooking.CookingScreen
import com.kronos.skilletapp.ui.screen.recipelist.RecipeListScreen
import com.kronos.skilletapp.ui.screen.recipe.RecipeScreen
import com.kronos.skilletapp.utils.navTypeOf
import kotlin.reflect.typeOf

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
    composable<Route.RecipeList>(
      deepLinks = listOf(
        navDeepLink {
          action = Intent.ACTION_SEND
          mimeType = "text/*"
        }
      )
    ) {
      RecipeListScreen(
        onNewRecipe = { navActions.navigateToAddEditRecipe("Add Recipe") },
        onNewRecipeByUrl = { navActions.navigateToAddEditRecipe(title = "Add Recipe", url = it) },
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
        onBack = { navController.popBackStack(route = Route.RecipeList, inclusive = false, saveState = true) },
        onEdit = { navActions.navigateToAddEditRecipe("Edit Recipe", args.recipeId) },
        onCook = { scale ->
          navActions.navigateToCooking(
            recipeId = args.recipeId,
            scale = scale
          )
        }
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
    composable<Route.Cooking>(
      typeMap = mapOf(typeOf<LinkedHashMap<String, String?>>() to navTypeOf<LinkedHashMap<String, String>>()),
    ) { backStackEntry ->
      val args = backStackEntry.toRoute<Route.Cooking>()
      CookingScreen(
        onBack = { navController.popBackStack() },
      )
    }
  }
}