package com.kronos.skilletapp

import android.R.attr.mimeType
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavDeepLinkRequest.Builder.Companion.fromAction
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.navOptions
import androidx.navigation.toRoute
import com.kronos.skilletapp.ui.screen.AddEditRecipeScreen
import com.kronos.skilletapp.ui.screen.cooking.CookingScreen
import com.kronos.skilletapp.ui.screen.recipelist.RecipeListScreen
import com.kronos.skilletapp.ui.screen.recipe.RecipeScreen
import com.kronos.skilletapp.utils.navDeepLinkRequest
import com.kronos.skilletapp.utils.navTypeOf
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import kotlin.reflect.typeOf
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val baseUrl = "skilletapp://skillet"

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
      if (intent.action != Intent.ACTION_SEND) return@collectLatest

      val sharedUrl = intent.getStringExtra(Intent.EXTRA_TEXT)
      intent.action?.let { intentAction ->
        navController.navigate(
          request = navDeepLinkRequest(uri = "${baseUrl}/recipeList?sharedUrl=$sharedUrl?urlId=${Uuid.random()}".toUri()) {
            action = intentAction
            mimeType = "text/*"
          },
          navOptions = navOptions { launchSingleTop = true }
        )
      }
    }
  }

  NavHost(
    navController = navController,
    startDestination = startDestination,
    popExitTransition = {
      slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End) + fadeOut()
    },
  ) {
    composable<Route.RecipeList>(
      deepLinks = listOf(
        navDeepLink<Route.RecipeList>(basePath = "${baseUrl}/recipeList") {
          action = Intent.ACTION_SEND
          mimeType = "text/*"
        }
      )
    ) {
      val args = it.toRoute<Route.RecipeList>()
      RecipeListScreen(
        onNewRecipe = { navActions.navigateToAddEditRecipe("Add Recipe") },
        onNewRecipeByUrl = { navActions.navigateToAddEditRecipe(title = "Add Recipe", url = it) },
        onRecipeClick = { navActions.navigateToRecipe(it) },
        vm = koinViewModel(key = args.sharedUrl + args.urlId),
        sharedUrl = args.sharedUrl
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
    composable<Route.Cooking>(
      typeMap = mapOf(typeOf<LinkedHashMap<String, String?>>() to navTypeOf<LinkedHashMap<String, String>>()),
    ) { backStackEntry ->
      val args = backStackEntry.toRoute<Route.Cooking>()
      CookingScreen(
        onBack = { navController.navigateUp() },
      )
    }
  }
}