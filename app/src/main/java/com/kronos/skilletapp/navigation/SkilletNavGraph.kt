package com.kronos.skilletapp.navigation

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.kronos.skilletapp.ui.screen.recipe.RecipeScreen
import com.kronos.skilletapp.ui.screen.recipelist.RecipeListScreen
import com.kronos.skilletapp.utils.navDeepLinkRequest
import com.kronos.skilletapp.utils.toJson
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
fun SkilletNavGraph(
  intentFlow: SharedFlow<Intent>,
  modifier: Modifier = Modifier,
  navController: NavHostController = rememberNavController(),
  startDestination: Route = Route.RecipeList(),
  navActions: SkilletNavigationActions = remember(navController) { SkilletNavigationActions(navController) },
) {
  LaunchedEffect(intentFlow) {
    intentFlow.collectLatest { intent ->
      if (intent.action == Intent.ACTION_SEND) {
        val sharedRecipe = intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
          SharedRecipe(
            url = it,
            id = Uuid.random().toString()
          )
        }

        intent.action?.let { intentAction ->
          val json = sharedRecipe?.toJson()
          val uri = Route.RecipeList.buildUri(json)

          navController.handleDeepLink(
            request = navDeepLinkRequest(uri) {
              action = intentAction
              mimeType = "text/*"
            }
          )
        }
      }
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
          action = Intent.ACTION_SEND
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