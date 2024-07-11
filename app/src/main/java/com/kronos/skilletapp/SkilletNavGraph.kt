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
  startDestination: Routes = Routes.RecipeList,
  navActions: SkilletNavigationActions = remember(navController) { SkilletNavigationActions(navController) },
) {
  NavHost(navController = navController, startDestination = startDestination) {
    composable<Routes.RecipeList> {
      RecipeListScreen()
    }
    composable<Routes.Recipe> { backStackEntry ->
      val route = backStackEntry.toRoute<Routes.Recipe>()
      RecipeScreen(id = route.recipeId)
    }
    composable<Routes.AddEditRecipe> { backStackEntry ->
      val recipeId = backStackEntry.toRoute<Routes.AddEditRecipe>().recipeId
//      AddEditRecipeScreen(recipeId = recipeId)
    }
  }
}