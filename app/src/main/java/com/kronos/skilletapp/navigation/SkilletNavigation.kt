package com.kronos.skilletapp.navigation

import android.net.Uri
import android.util.Log.d
import androidx.compose.runtime.compositionLocalOf
import androidx.core.net.toUri
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.kronos.skilletapp.navigation.toRouteString
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

@Serializable
data class SharedRecipe(val url: String, val id: String)

@Serializable
sealed interface Route {
  @Serializable data class RecipeList(val sharedRecipe: SharedRecipe? = null) : Route
  @Serializable data class Recipe(val recipeId: String) : Route
  @Serializable data class AddEditRecipe(val title: String, val recipeId: String? = null, val url: String? = null) : Route
  @Serializable data class Cooking(val recipeId: String, val scale: Float) : Route

  companion object {
    const val BASE_URL = "skilletapp://skillet"

    fun basePath(route: KClass<out Route>): String = "$BASE_URL/" + when (route) {
      Route.RecipeList::class -> "recipeList"
      Route.Recipe::class -> "recipe"
      Route.AddEditRecipe::class -> "addEditRecipe"
      Route.Cooking::class -> "cooking"
      else -> throw UnsupportedOperationException("Unknown route: $route")
    }
  }
}

fun KClass<out Route>.toRouteString(): String {
  val basePath = Route.basePath(this)
  val properties = memberProperties
  return buildString {
    append(basePath)

    properties.groupBy { it.returnType.isMarkedNullable }.let { grouped ->
      val nonNullableProperties = grouped[false] ?: emptyList()
      val nullableProperties = grouped[true] ?: emptyList()
      var count = 0

      nonNullableProperties.forEach { property ->
        append("/${count++}")
      }

      nullableProperties.forEach { property ->
        append("?${property.name}={${count++}}")
      }
    }
  }
}

fun buildUri(route: KClass<out Route>, vararg args: Any?): Uri {
  var result = route.toRouteString()

  args.forEachIndexed { index, arg ->
    result = result.replace("{$index}", arg.toString())
  }

  result = result.replace("\\{\\d+\\}".toRegex(), "null")

  return result.toUri()
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

val LocalNavigationActions = compositionLocalOf<SkilletNavigationActions> { error("No SkilletNavigationActions provided") }
val LocalNavController = compositionLocalOf<NavHostController> { error("No NavController provided") }