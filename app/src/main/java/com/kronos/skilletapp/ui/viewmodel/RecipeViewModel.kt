package com.kronos.skilletapp.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.github.michaelbull.result.*
import com.kronos.skilletapp.Route
import com.kronos.skilletapp.data.RecipeRepository
import com.kronos.skilletapp.data.SkilletError
import com.kronos.skilletapp.data.UiState
import com.kronos.skilletapp.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class RecipeUiState(
  val recipe: Recipe,
)

class RecipeViewModel(
  private val recipeRepository: RecipeRepository,
  private val handle: SavedStateHandle
) : ViewModel() {

  private val args = handle.toRoute<Route.Recipe>()
  private val recipeId = args.recipeId

  private val _selectedUnits = mutableStateMapOf<Ingredient, MeasurementUnit?>()
  val selectedUnits = snapshotFlow { _selectedUnits.toMap() }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000L),
    initialValue = emptyMap()
  )

  private val _isLoading = MutableStateFlow(false)
  private val _recipeAsync = recipeRepository.fetchRecipeStream(recipeId)
    .map { result ->
      result.mapBoth(
        success = { UiState.Success(it) },
        failure = { UiState.Error(it) }
      )
    }
    .catch { emit(UiState.Error(SkilletError(it.message?: "Unknown error"))) }

  val uiState = combine(_isLoading, _recipeAsync) { loading, recipeAsync ->
    when {
      loading -> UiState.Loading
      else -> when (recipeAsync) {
        UiState.Loading -> UiState.Loading
        is UiState.Error -> recipeAsync
        is UiState.Success -> UiState.Success(
          RecipeUiState(
            recipe = recipeAsync.data,
          )
        )
      }
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000L),
    initialValue = UiState.Loading
  )

  fun selectUnit(ingredient: Ingredient, unit: MeasurementUnit?) {
    _selectedUnits[ingredient] = unit
  }

  fun getSelectedUnit(ingredient: Ingredient): MeasurementUnit? = _selectedUnits[ingredient]

  fun refresh() {
    _isLoading.value = true
    viewModelScope.launch {
//      recipeRepository.refreshRecipe()
      _isLoading.value = false
    }
  }
}