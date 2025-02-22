package com.kronos.skilletapp.ui.viewmodel

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.kronos.skilletapp.data.RecipeRepository
import com.kronos.skilletapp.data.SkilletError
import com.kronos.skilletapp.data.UiState
import com.kronos.skilletapp.model.Recipe
import com.kronos.skilletapp.ui.screen.recipelist.RecipesSortType
import kotlinx.coroutines.flow.*

data class RecipeListState(
  val recipes: List<Recipe>,
  val isRefreshing: Boolean = false,
  val sharedUrl: String = ""
)

class RecipeListViewModel(
  private val recipeRepository: RecipeRepository,
  private val handle: SavedStateHandle
) : ViewModel() {
  private val _savedSortType = handle.getStateFlow(RECIPES_SORT_TYPE_KEY, RecipesSortType.NAME)

  private val _isLoading = MutableStateFlow(false)
  private val _recipesAsync = recipeRepository.observeRecipes()
    .distinctUntilChanged()
    .map { UiState.LoadedWithData(it) }
    .catch<UiState<List<Recipe>>> { emit(UiState.Error(SkilletError("Error loading recipes"))) }

  private val _sharedUrl = handle.getStateFlow(NavController.KEY_DEEP_LINK_INTENT, Intent())
    .map {
      if (it.action != Intent.ACTION_SEND) return@map ""

      it.getStringExtra(Intent.EXTRA_TEXT) ?: ""
    }

  val uiState = combine(_isLoading, _sharedUrl, _recipesAsync) { isLoading, sharedUrl, recipesAsync ->
    when {
      isLoading -> UiState.Loading
      else -> when (recipesAsync) {
        UiState.Loading -> UiState.Loading
        is UiState.Error -> UiState.Error(recipesAsync.error)
        is UiState.LoadedWithData -> UiState.LoadedWithData(
          RecipeListState(
            recipes = recipesAsync.data,
            sharedUrl = sharedUrl
          )
        )

        else -> UiState.Error(SkilletError("UiState.Loaded should not be used here"))
      }
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

}

const val RECIPES_SORT_TYPE_KEY = "RECIPES_SORT_TYPE_KEY"
