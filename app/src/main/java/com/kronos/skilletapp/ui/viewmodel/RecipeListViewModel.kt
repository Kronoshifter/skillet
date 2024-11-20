package com.kronos.skilletapp.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kronos.skilletapp.data.RecipeRepository
import com.kronos.skilletapp.data.SkilletError
import com.kronos.skilletapp.data.UiState
import com.kronos.skilletapp.model.Recipe
import com.kronos.skilletapp.ui.screen.recipelist.RecipesSortType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RecipeListState(
  val recipes: List<Recipe>,
  val isRefreshing: Boolean = false,
)

class RecipeListViewModel(
  private val recipeRepository: RecipeRepository,
  private val handle: SavedStateHandle
) : ViewModel() {
  private val _isRefreshing = MutableStateFlow(false)
  val isRefreshing = _isRefreshing.asStateFlow()

  private val _savedSortType = handle.getStateFlow(RECIPES_SORT_TYPE_KEY, RecipesSortType.NAME)

  private val _isLoading = MutableStateFlow(false)
  private val _recipesAsync = recipeRepository.fetchRecipesFromDatabase() //TODO: fetch from database
    .distinctUntilChanged()
    .map { UiState.LoadedWithData(it) }
    .catch<UiState<List<Recipe>>> { emit(UiState.Error(SkilletError("Error loading recipes"))) }

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

  fun refresh() {
    viewModelScope.launch {
      _isRefreshing.value = true
      recipeRepository.refreshRecipes()
    }.invokeOnCompletion { _isRefreshing.value = false }
  }
}

const val RECIPES_SORT_TYPE_KEY = "RECIPES_SORT_TYPE_KEY"
