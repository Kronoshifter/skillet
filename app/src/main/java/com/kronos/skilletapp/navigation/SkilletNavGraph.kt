package com.kronos.skilletapp.navigation

import android.content.Intent
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.kronos.skilletapp.ui.screen.AddEditRecipeScreen
import com.kronos.skilletapp.ui.screen.cooking.CookingScreen
import com.kronos.skilletapp.ui.screen.recipe.RecipeScreen
import com.kronos.skilletapp.ui.screen.recipelist.RecipeListScreen
import com.kronos.skilletapp.utils.isValidUrl
import com.kronos.skilletapp.utils.navDeepLinkRequest
import com.kronos.skilletapp.utils.toJson
import kotlinx.coroutines.flow.*
import org.koin.androidx.compose.koinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
fun SkilletNavGraph(
  newIntent: Intent?,
  modifier: Modifier = Modifier,
  navController: NavHostController = rememberNavController(),
  startDestination: Route = Route.RecipeList(),
  navActions: SkilletNavigationActions = remember(navController) { SkilletNavigationActions(navController) },
) {
  val currentIntent by rememberUpdatedState(newIntent)
  LaunchedEffect(Unit) {
    snapshotFlow { currentIntent }
      .filterNotNull()
      .filter { it.action == Intent.ACTION_SEND }
      .filter { it.type?.startsWith("text/") == true }
      .filter { it.hasExtra(Intent.EXTRA_TEXT) }
      .map { it.getCharSequenceExtra(Intent.EXTRA_TEXT).toString() }
      .filterNotNull()
      .filter { it.isValidUrl() }
      .map {
        SharedRecipe(
          url = it,
          id = Uuid.random().toString()
        )
      }.collect {
        Log.d("SHARED_RECIPE", "Extracting data from intent: $it")
        val uri = Route.RecipeList.buildUri(it.toJson())
        navController.handleDeepLink(
          request = navDeepLinkRequest(uri) {
            action = Route.RecipeList.INTENT_EXTRA_SHARED_RECIPE
            mimeType = "text/*"
          }
        )
      }
  }

  NavHost(
    navController = navController,
    modifier = modifier,
    startDestination = startDestination,
  ) {
    composable<Route.RecipeList>(
      typeMap = Route.RecipeList.typeMap,
      deepLinks = listOf(
        navDeepLink<Route.RecipeList>(
          basePath = Route.RecipeList.basePath,
          typeMap = Route.RecipeList.typeMap,
        ) {
          action = Route.RecipeList.INTENT_EXTRA_SHARED_RECIPE
          mimeType = "text/*"
        }
      )
    ) { backStackEntry ->
      val args = backStackEntry.toRoute<Route.RecipeList>()
      RecipeListScreen(
        onNewRecipe = { navActions.navigateToAddEditRecipe("Add Recipe") },
        onNewRecipeByUrl = { navActions.navigateToAddEditRecipe(title = "Add Recipe", url = it) },
        onRecipeClick = { navActions.navigateToRecipe(it) },
        vm = koinViewModel(key = args.sharedRecipe.toString()),
      )
    }

    composable<Route.Recipe> {
      val args = it.toRoute<Route.Recipe>()
      RecipeScreen(
        onBack = { navController.navigateUp() },
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
        onBack = { navController.navigateUp() },
        onRecipeUpdate = { recipeId ->
          navActions.navigateToRecipe(recipeId)
        },
      )
    }

    composable<Route.Cooking> { backStackEntry ->
      val args = backStackEntry.toRoute<Route.Cooking>()
      CookingScreen(
        onBack = { navController.navigateUp() },
      )
    }
  }
}