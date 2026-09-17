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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecipeListViewModel(
  private val recipeRepository: RecipeRepository,
  private val handle: SavedStateHandle,
) : ViewModel() {
  private val _savedSortType = handle.getStateFlow(RECIPES_SORT_TYPE_KEY, RecipesSortType.NAME)

  private val args =
    handle.toRoute<Route.RecipeList>(
      typeMap = mapOf(typeOf<SharedRecipe?>() to navTypeOf<SharedRecipe?>(true))
    )

  @OptIn(SavedStateHandleSaveableApi::class)
  var sharedRecipe by
    handle.saveable(stateSaver = saverOf<SharedRecipe?>()) {
      mutableStateOf<SharedRecipe?>(
        args.sharedRecipe?.let {
          it.copy(url = URLDecoder.decode(it.url, StandardCharsets.UTF_8.toString()))
        }
      )
    }

  @OptIn(SavedStateHandleSaveableApi::class)
  var showSharedUrl by handle.saveable { mutableStateOf(true) }

  private val recipeListAsync = recipeRepository.observeRecipeSummaries()
    .map { Async.Success(it) }
    .catch { Async.Failure(RecipeCouldNotBeLoadedError("Could not load recipes")) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), Async.Idle)

  private val _listState = MutableStateFlow<Async<List<RecipeSummary>>>(Async.idle())
  val listState: StateFlow<Async<List<RecipeSummary>>> = _listState.asStateFlow()

  init {
    viewModelScope.launch {
      try {
        recipeRepository
          .observeRecipeSummaries()
          .distinctUntilChanged()
          .collect { summaries ->
            _listState.update { Async.success(summaries) }
          }
      } catch (e: Exception) {
        Log.e("RecipeListViewModel", "Error loading recipes", e)
        _listState.update { Async.failure(RecipeCouldNotBeLoadedError("Could not load recipes")) }
      }
    }
  }
}

const val RECIPES_SORT_TYPE_KEY = "RECIPES_SORT_TYPE_KEY"
