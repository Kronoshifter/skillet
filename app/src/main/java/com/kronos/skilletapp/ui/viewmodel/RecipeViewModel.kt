package com.kronos.skilletapp.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.*
import com.kronos.skilletapp.data.RecipeRepository
import com.kronos.skilletapp.data.SkilletError
import com.kronos.skilletapp.model.*
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipeViewModel(
  private val recipeRepository: RecipeRepository
) : ViewModel() {
  private val selectedUnits = mutableStateMapOf<Ingredient, MeasurementUnit?>()

  private var _recipe = MutableStateFlow<Result<Recipe, SkilletError>>(Err(SkilletError("No recipe found")))
  val recipe: StateFlow<Result<Recipe, SkilletError>> = _recipe.asStateFlow()

  var loading: Boolean by mutableStateOf(true)
    private set

  lateinit var recipeId: String

  fun selectUnit(ingredient: Ingredient, unit: MeasurementUnit?) {
    selectedUnits[ingredient] = unit
  }

  fun getSelectedUnit(ingredient: Ingredient): MeasurementUnit? {
    return selectedUnits[ingredient]
  }

  fun fetchRecipe(id: String) {
    loading = true
    viewModelScope.launch {
      val result = async { recipeRepository.fetchRecipe(id) }
      _recipe.value = result.await()
      recipeId = id
      loading = false
    }
  }

  fun refresh() {
    fetchRecipe(recipeId)
  }
}