package com.kronos.skilletapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.kronos.skilletapp.data.RecipeCouldNotBeLoadedError
import com.kronos.skilletapp.navigation.Route
import com.kronos.skilletapp.data.RecipeRepository
import com.kronos.skilletapp.data.SkilletError
import com.kronos.skilletapp.data.UiState
import com.kronos.skilletapp.data.UsedLoadedWhereYouShouldntError
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.measurement.MeasurementUnit
import com.kronos.skilletapp.model.Recipe
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RecipeUiState(
  val servings: Int = 1,
  val selectedUnits: Map<Ingredient, MeasurementUnit?> = emptyMap(),
  val scale: Float = 1f,
)

class RecipeViewModel(
  private val recipeRepository: RecipeRepository,
  private val handle: SavedStateHandle,
) : ViewModel() {

  private val args = handle.toRoute<Route.Recipe>()
  private val recipeId = args.recipeId

  private val _uiState = MutableStateFlow(RecipeUiState())
  val uiState = _uiState.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  private val _recipeAsync = recipeRepository.observeRecipe(recipeId)
    .map { UiState.LoadedWithData(it) }
    .catch<UiState<Recipe>> {
      Log.e("RecipeScreen", it.message, it)
      emit(UiState.Error(RecipeCouldNotBeLoadedError("Could not load recipe")))
    }

  val recipeState = combine(_isLoading, _recipeAsync) { loading, recipeAsync ->
    when {
      loading -> UiState.Loading
      else -> when (recipeAsync) {
        UiState.Loading -> UiState.Loading
        is UiState.Error -> recipeAsync
        is UiState.LoadedWithData -> {
          if (_uiState.value.scale == 1f) {
            _uiState.update { it.copy(servings = recipeAsync.data.servings) }
          }
          UiState.LoadedWithData(recipeAsync.data)
        }
        else -> UiState.Error(UsedLoadedWhereYouShouldntError)
      }
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000L),
    initialValue = UiState.Loading
  )

  fun selectUnit(ingredient: Ingredient, unit: MeasurementUnit?) {
    _uiState.update {
      it.copy(selectedUnits = it.selectedUnits + (ingredient to unit))
    }
  }

  fun setScaling(scale: Float, servings: Int) {
    _uiState.update { it.copy(servings = servings, scale = scale) }
  }

  fun refresh() {
    _isLoading.value = true
    viewModelScope.launch {
//      recipeRepository.refreshRecipe()
      _isLoading.value = false
    }
  }
}