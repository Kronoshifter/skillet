package com.kronos.skilletapp.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.navigation.toRoute
import com.kronos.skilletapp.navigation.Route
import com.kronos.skilletapp.navigation.SharedRecipe
import com.kronos.skilletapp.data.RecipeRepository
import com.kronos.skilletapp.data.SkilletError
import com.kronos.skilletapp.data.UiState
import com.kronos.skilletapp.model.Recipe
import com.kronos.skilletapp.ui.saverOf
import com.kronos.skilletapp.ui.screen.recipelist.RecipesSortType
import com.kronos.skilletapp.utils.navTypeOf
import kotlinx.coroutines.flow.*
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlin.reflect.typeOf

data class RecipeListState(
  val recipes: List<Recipe>,
)

class RecipeListViewModel(
  private val recipeRepository: RecipeRepository,
  private val handle: SavedStateHandle
) : ViewModel() {
  private val _savedSortType = handle.getStateFlow(RECIPES_SORT_TYPE_KEY, RecipesSortType.NAME)

  private val args = handle.toRoute<Route.RecipeList>(typeMap = mapOf(typeOf<SharedRecipe?>() to navTypeOf<SharedRecipe?>(true)))

  @OptIn(SavedStateHandleSaveableApi::class)
  var sharedRecipe by handle.saveable(stateSaver = saverOf<SharedRecipe?>()) {
    mutableStateOf<SharedRecipe?>(args.sharedRecipe?.let {
      it.copy(
        url = URLDecoder.decode(it.url, StandardCharsets.UTF_8.toString())
      )
    })
  }

  @OptIn(SavedStateHandleSaveableApi::class)
  var showSharedUrl by handle.saveable {
    mutableStateOf(true)
  }

  private val _isLoading = MutableStateFlow(false)
  private val _recipesAsync = recipeRepository.observeRecipes()
    .distinctUntilChanged()
    .map { UiState.LoadedWithData(it) }
    .catch<UiState<List<Recipe>>> {
      Log.e("RecipeListViewModel", "Error loading recipes", it)
      emit(UiState.Error(SkilletError("Error loading recipes")))
    }

  val uiState = combine(_isLoading, _recipesAsync) { isLoading, recipesAsync ->
    when {
      isLoading -> UiState.Loading
      else -> when (recipesAsync) {
        UiState.Loading -> UiState.Loading
        is UiState.Error -> UiState.Error(recipesAsync.error)
        is UiState.LoadedWithData -> UiState.LoadedWithData(
          RecipeListState(
            recipes = recipesAsync.data,
          )
        )

        else -> UiState.Error(SkilletError("UiState.Loaded should not be used here"))
      }
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)
}

const val RECIPES_SORT_TYPE_KEY = "RECIPES_SORT_TYPE_KEY"