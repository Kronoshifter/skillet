package com.kronos.skilletapp.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.kronos.measurement.model.MeasurementUnit
import com.kronos.skilletapp.data.Async
import com.kronos.skilletapp.data.RecipeRepository
import com.kronos.skilletapp.domain.scaling.ScaleRecipe
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.Recipe
import com.kronos.skilletapp.model.RecipeCouldNotBeLoadedError
import com.kronos.skilletapp.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class RecipeUiState(
  val selectedUnits: Map<Ingredient, MeasurementUnit?> = emptyMap(),
  val currentServings: Int = 1,
  val scaledIngredients: List<Ingredient> = emptyList(),
)

class RecipeViewModel(
  private val recipeRepository: RecipeRepository,
  private val handle: SavedStateHandle,
  private val scaleRecipe: ScaleRecipe,
) : ViewModel() {

  private val args = handle.toRoute<Route.Recipe>()
  private val recipeId = args.recipeId

  private var originalRecipe: Recipe? = null

  private val _uiState = MutableStateFlow(RecipeUiState())
  val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

  val recipeAsync: StateFlow<Async<Recipe>> =
    recipeRepository
      .observeRecipe(recipeId)
      .onEach { recipe ->
        if (originalRecipe == null) {
          originalRecipe = recipe
          _uiState.update {
            it.copy(
              currentServings = recipe.servings,
              scaledIngredients = scaleRecipe(recipe, recipe.servings).scaledIngredients,
            )
          }
        }
      }
      .map { Async.Success(it) as Async<Recipe> }
      .catch { emit(Async.Failure(RecipeCouldNotBeLoadedError("Could not load recipe"))) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), Async.Idle)

  fun selectUnit(ingredient: Ingredient, unit: MeasurementUnit?) {
    _uiState.update { it.copy(selectedUnits = it.selectedUnits + (ingredient to unit)) }
  }

  fun setScaling(servings: Int) {
    originalRecipe?.let { original ->
      _uiState.update {
        it.copy(
          currentServings = servings,
          scaledIngredients = scaleRecipe(original, servings).scaledIngredients,
        )
      }
    }
  }
}
