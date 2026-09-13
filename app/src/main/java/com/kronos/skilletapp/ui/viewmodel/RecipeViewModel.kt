package com.kronos.skilletapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.kronos.skilletapp.data.RecipeRepository
import com.kronos.skilletapp.data.UiState
import com.kronos.skilletapp.domain.scaling.ScaleRecipe
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.Recipe
import com.kronos.skilletapp.model.RecipeCouldNotBeLoadedError
import com.kronos.skilletapp.model.UsedLoadedWhereYouShouldntError
import com.kronos.measurement.model.MeasurementUnit
import com.kronos.skilletapp.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecipeUiState(
  val selectedUnits: Map<Ingredient, MeasurementUnit?> = emptyMap(),
  val currentServings: Int = 1,
  val scaledIngredients: List<Ingredient> = emptyList(),
  val originalRecipe: Recipe? = null,
)

class RecipeViewModel(
  private val recipeRepository: RecipeRepository,
  private val handle: SavedStateHandle,
  private val scaleRecipe: ScaleRecipe,
) : ViewModel() {

  private val args = handle.toRoute<Route.Recipe>()
  private val recipeId = args.recipeId

  private val _uiState = MutableStateFlow(RecipeUiState())
  val uiState = _uiState.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  private val _recipeAsync =
    recipeRepository
      .observeRecipe(recipeId)
      .map { UiState.LoadedWithData(it) }
      .catch<UiState<Recipe>> {
        Log.e("RecipeScreen", it.message, it)
        emit(UiState.Error(RecipeCouldNotBeLoadedError("Could not load recipe")))
      }

  val recipeState =
    combine(_isLoading, _recipeAsync) { loading, recipeAsync ->
        when {
          loading -> UiState.Loading
          else ->
            when (recipeAsync) {
              UiState.Loading -> UiState.Loading
              is UiState.Error -> recipeAsync
              is UiState.LoadedWithData -> {
                if (_uiState.value.originalRecipe == null) {
                  val originalRecipe = recipeAsync.data
                  val scaled = scaleRecipe(originalRecipe, originalRecipe.servings)
                  _uiState.update {
                    it.copy(
                      currentServings = originalRecipe.servings,
                      scaledIngredients = scaled.scaledIngredients,
                      originalRecipe = originalRecipe,
                    )
                  }
                }
                UiState.LoadedWithData(recipeAsync.data)
              }
              else -> UiState.Error(UsedLoadedWhereYouShouldntError)
            }
        }
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = UiState.Loading,
      )

  fun selectUnit(ingredient: Ingredient, unit: MeasurementUnit?) {
    _uiState.update { it.copy(selectedUnits = it.selectedUnits + (ingredient to unit)) }
  }

  fun setScaling(servings: Int) {
    val originalRecipe = _uiState.value.originalRecipe ?: return
    val scaled = scaleRecipe(originalRecipe, servings)
    _uiState.update {
      it.copy(
        currentServings = servings,
        scaledIngredients = scaled.scaledIngredients,
      )
    }
  }

  fun refresh() {
    _isLoading.value = true
    viewModelScope.launch {
      //      recipeRepository.refreshRecipe()
      _isLoading.value = false
    }
  }
}
