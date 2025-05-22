package com.kronos.skilletapp.navigation

import android.net.Uri
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import com.kronos.skilletapp.utils.navMapEntryOf
import com.kronos.skilletapp.utils.navTypeOf
import kotlinx.serialization.Serializable
import kotlin.reflect.KType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.typeOf

@Serializable
data class SharedRecipe(val url: String, val id: String)

interface RouteInfo<out R : Route> {
  val routeId: String

  val basePath: String
    get() = Route.BASE_URL + routeId

  val typeMap: Map<KType, @JvmSuppressWildcards NavType<*>>
    get() = emptyMap()

}

@Serializable
sealed interface Route {
  val routeId: String
  
  @Serializable
  data class RecipeList(val sharedRecipe: SharedRecipe? = null) : Route {
    override val routeId: String by RecipeList::routeId
    companion object : RouteInfo<RecipeList> {
      override val routeId = "recipeList"
      override val typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = mapOf(navMapEntryOf<SharedRecipe?>(true))

      const val INTENT_EXTRA_SHARED_RECIPE = "sharedRecipe"
    }
  }

  @Serializable
  data class Recipe(val recipeId: String) : Route {
    override val routeId: String by Recipe::routeId

    companion object : RouteInfo<Recipe> {
      override val routeId = "recipe"
    }
  }

  @Serializable
  data class AddEditRecipe(val title: String, val recipeId: String? = null, val url: String? = null) : Route {
    override val routeId: String by AddEditRecipe::routeId

    companion object : RouteInfo<AddEditRecipe> {
      override val routeId = "addEditRecipe"
    }
  }

  @Serializable
  data class Cooking(val recipeId: String, val scale: Float) : Route {
    override val routeId: String by Cooking::routeId

    companion object : RouteInfo<Cooking> {
      override val routeId = "cooking"
    }
  }

  companion object {
    const val SCHEME = "skilletapp"
    const val AUTHORITY = "skillet"
    const val BASE_URL = "$SCHEME://$AUTHORITY/"
  }
}

inline fun <reified R : Route, reified T : R> RouteInfo<T>.buildUri(vararg args: Any?): Uri = Uri.Builder().apply {
  scheme(Route.SCHEME)
  authority(Route.AUTHORITY)
  appendPath(routeId)

  T::class.memberProperties.filterNot { it.name == "routeId" }.groupBy { it.returnType.isMarkedNullable }.let { grouped ->
    val nonNullableProperties = grouped[false] ?: emptyList()
    val nullableProperties = grouped[true] ?: emptyList()

    require(args.size >= nonNullableProperties.size) {
      "Not enough arguments provided for route $this"
    }

    require(args.size <= nonNullableProperties.size + nullableProperties.size) {
      "Too many arguments provided for route $this"
    }

    args.slice(0 until nonNullableProperties.size).forEach { arg ->
      appendPath(arg.toString())
    }

    args.slice(nonNullableProperties.size until args.size).forEachIndexed { index, arg ->
      appendQueryParameter(nullableProperties[index].name, arg.toString())
    }

    nullableProperties.slice((args.size - nonNullableProperties.size) until nullableProperties.size).forEach { property ->
      appendQueryParameter(property.name, "null")
    }
  }
}.build()

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