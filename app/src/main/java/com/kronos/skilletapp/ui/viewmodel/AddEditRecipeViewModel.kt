package com.kronos.skilletapp.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.github.michaelbull.result.mapBoth
import com.github.michaelbull.result.mapOrElse
import com.kronos.skilletapp.Route
import com.kronos.skilletapp.data.RecipeRepository
import com.kronos.skilletapp.data.UiState
import com.kronos.skilletapp.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddEditRecipeUiState(
//  val recipe: Recipe? = null, //TODO: split into components instead of full recipe object
  val name: String = "",
  val description: String = "",
  val notes: String = "",
  val servings: Int = 0,
  val prepTime: Int = 0,
  val cookTime: Int = 0,
  val ingredients: List<Ingredient> = emptyList(),
  val instructions: List<Instruction> = emptyList(),
  val equipment: List<Equipment> = emptyList(),
)

class AddEditRecipeViewModel(
  private val recipeRepository: RecipeRepository,
  handle: SavedStateHandle,
) : ViewModel() {
  private val args = handle.toRoute<Route.AddEditRecipe>()
  private val recipeId = args.recipeId

  private val _uiState: MutableStateFlow<UiState<AddEditRecipeUiState>> = MutableStateFlow(UiState.Loading)
  val uiState = _uiState.asStateFlow()

  init {
    recipeId?.let {
      loadRecipe(it)
    }
  }

  private fun loadRecipe(id: String) {
    _uiState.update {
      UiState.Loading
    }
    viewModelScope.launch {
      _uiState.update {
        recipeRepository.fetchRecipe(id).mapOrElse({ error -> UiState.Error(error)}) { recipe ->
          UiState.Success(
            AddEditRecipeUiState(
              name = recipe.name,
              description = recipe.description,
              notes = recipe.notes,
              servings = recipe.servings,
              prepTime = recipe.time.preparation,
              cookTime = recipe.time.cooking,
              ingredients = recipe.ingredients,
              instructions = recipe.instructions,
              equipment = recipe.equipment
            )
          )
        }
      }
    }
  }
}