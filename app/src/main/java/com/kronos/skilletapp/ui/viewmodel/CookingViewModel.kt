package com.kronos.skilletapp.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapOrElse
import com.kronos.skilletapp.Route
import com.kronos.skilletapp.data.RecipeRepository
import com.kronos.skilletapp.data.SkilletError
import com.kronos.skilletapp.data.UiState
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.MeasurementUnit
import com.kronos.skilletapp.model.Recipe
import kotlinx.coroutines.flow.*

class CookingViewModel(
  private val recipeRepository: RecipeRepository,
  private val handle: SavedStateHandle
) : ViewModel() {
  private val args = handle.toRoute<Route.Cooking>()
  private val recipeId = args.recipeId

//  private val _selectedUnits = MutableStateFlow(args.selectedUnits)
  private val _selectedUnits = MutableStateFlow<Map<Ingredient, MeasurementUnit?>>(emptyMap())
  val selectedUnits = _selectedUnits.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  private val _recipeAsync = recipeRepository.fetchRecipeStream(recipeId)
    .map { result ->
      result.mapOrElse({ UiState.Error(it) }) {
        UiState.LoadedWithData(it)
      }
    }
    .catch { emit(UiState.Error(SkilletError(it.message ?: "Unknown error"))) }

  val uiState = combine(_isLoading, _recipeAsync) { loading, recipeAsync ->
    when {
      loading -> UiState.Loading
      else -> when (recipeAsync) {
        UiState.Loading -> UiState.Loading
        is UiState.Error -> recipeAsync
        is UiState.LoadedWithData -> UiState.LoadedWithData(
          recipeAsync.data
        )
        else -> UiState.Error(SkilletError("UiState.Loaded should not be used here"))
      }
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000L),
    initialValue = UiState.Loading
  )

  fun selectUnit(ingredient: Ingredient, unit: MeasurementUnit?) {
    _selectedUnits.update {
      it + (ingredient to unit)
    }
  }
}