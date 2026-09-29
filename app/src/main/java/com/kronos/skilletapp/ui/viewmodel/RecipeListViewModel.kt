package com.kronos.skilletapp.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.navigation.toRoute
import com.kronos.skilletapp.data.Async
import com.kronos.skilletapp.data.RecipeRepository
import com.kronos.skilletapp.model.RecipeCouldNotBeLoadedError
import com.kronos.skilletapp.model.RecipeSummary
import com.kronos.skilletapp.navigation.Route
import com.kronos.skilletapp.navigation.SharedRecipe
import com.kronos.skilletapp.ui.saverOf
import com.kronos.skilletapp.ui.screen.recipelist.RecipesSortType
import com.kronos.skilletapp.utils.navTypeOf
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlin.reflect.typeOf
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class RecipeListViewModel(
  private val recipeRepository: RecipeRepository,
  private val handle: SavedStateHandle,
) : ViewModel() {
  private val _savedSortType = handle.getStateFlow(RECIPES_SORT_TYPE_KEY, RecipesSortType.NAME)

  private val args = handle.toRoute<Route.RecipeList>(typeMap = mapOf(typeOf<SharedRecipe?>() to navTypeOf<SharedRecipe?>(true)))

  @OptIn(SavedStateHandleSaveableApi::class)
  var sharedRecipe by
    handle.saveable(stateSaver = saverOf<SharedRecipe?>()) {
      mutableStateOf<SharedRecipe?>(args.sharedRecipe?.let { it.copy(url = URLDecoder.decode(it.url, StandardCharsets.UTF_8.toString())) })
    }

  @OptIn(SavedStateHandleSaveableApi::class) var showSharedUrl by handle.saveable { mutableStateOf(true) }

  val recipeListAsync: StateFlow<Async<List<RecipeSummary>>> =
    recipeRepository
      .observeRecipeSummaries()
      .distinctUntilChanged()
      .map { Async.Success(it) as Async<List<RecipeSummary>> }
      .catch {
        Log.e("RecipeListViewModel", "Could not load recipes", it)
        emit(Async.Failure(RecipeCouldNotBeLoadedError("Could not load recipes")))
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), Async.Idle)
}

const val RECIPES_SORT_TYPE_KEY = "RECIPES_SORT_TYPE_KEY"
