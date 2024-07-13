package com.kronos.skilletapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kronos.skilletapp.data.RecipeRepository
import com.kronos.skilletapp.data.SkilletError
import com.kronos.skilletapp.data.UiState
import com.kronos.skilletapp.model.Recipe
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

data class RecipeListState(
  val recipes: List<Recipe>,
  val isRefreshing: Boolean = false,
)

class RecipeListViewModel(
  private val recipeRepository: RecipeRepository,
) : ViewModel() {
  private val _isRefreshing = MutableStateFlow(false)
  val isRefreshing = _isRefreshing.asStateFlow()
  private val _isLoading = MutableStateFlow(false)
  private val _recipesAsync = recipeRepository.fetchRecipesStream()
    .map { UiState.Success(it) }
    .catch<UiState<List<Recipe>>> { emit(UiState.Error(SkilletError("Error loading recipes"))) }

  val uiState = combine(_isLoading, _recipesAsync) { isLoading, recipesAsync ->
    when {
      isLoading -> UiState.Loading
      else -> when (recipesAsync) {
        UiState.Loading -> UiState.Loading
        is UiState.Error -> UiState.Error(recipesAsync.error)
        is UiState.Success -> UiState.Success(
          RecipeListState(
            recipes = recipesAsync.data,
          )
        )
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