package com.kronos.skilletapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kronos.skilletapp.data.RecipeRepository
import com.kronos.skilletapp.data.SkilletError
import com.kronos.skilletapp.data.UiState
import com.kronos.skilletapp.model.Recipe
import kotlinx.coroutines.flow.*

class RecipeListViewModel(
  private val recipeRepository: RecipeRepository
) : ViewModel() {

  private val _isLoading = MutableStateFlow(false)
  private val _recipesAsync = recipeRepository.fetchRecipesStream()
    .map { UiState.Success(it) }
    .catch<UiState<List<Recipe>>> { emit(UiState.Error(SkilletError("Error loading recipes"))) }

  val uiState: StateFlow<UiState<List<Recipe>>> = combine(_isLoading, _recipesAsync) { isLoading, recipesAsync ->
    when {
      isLoading -> UiState.Loading
      else -> when (recipesAsync) {
        UiState.Loading -> UiState.Loading
        is UiState.Error -> UiState.Error(recipesAsync.error)
        is UiState.Success -> UiState.Success(recipesAsync.data)
      }
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

}