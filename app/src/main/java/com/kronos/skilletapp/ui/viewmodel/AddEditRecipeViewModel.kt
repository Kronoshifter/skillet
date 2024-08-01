package com.kronos.skilletapp.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.github.michaelbull.result.onSuccess
import com.kronos.skilletapp.Route
import com.kronos.skilletapp.data.RecipeRepository
import com.kronos.skilletapp.data.UiState
import com.kronos.skilletapp.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RecipeState(
  val name: String = "",
  val description: String = "",
  val notes: String = "",
  val servings: Int = 0,
  val prepTime: Int = 0,
  val cookTime: Int = 0,
  val source: String = "",
  val sourceName: String = "",
  val ingredients: List<Ingredient> = emptyList(),
  val instructions: List<Instruction> = emptyList(),
  val equipment: List<Equipment> = emptyList(),
  val userMessage: String? = null,
  val isRecipeSaved: Boolean = false,
)

class AddEditRecipeViewModel(
  private val recipeRepository: RecipeRepository,
  handle: SavedStateHandle,
) : ViewModel() {
  private val args = handle.toRoute<Route.AddEditRecipe>()
  private val recipeId = args.recipeId
  private lateinit var createdId: String

  private val _uiState: MutableStateFlow<UiState<Unit>> = MutableStateFlow(UiState.Loaded(Unit))
  private val _recipeState: MutableStateFlow<RecipeState> = MutableStateFlow(RecipeState())
  val uiState = _uiState.asStateFlow()
  val recipeState = _recipeState.asStateFlow()

  init {
    recipeId?.let {
      loadRecipe(it)
    }
  }

  fun getRecipeId(): String {
    return recipeId ?: createdId
  }

  fun saveRecipe() {
    checkForInvalidForm()?.let { msg ->
      _recipeState.update {
        it.copy(userMessage = msg)
      }
      return
    }

    if (recipeId == null) {
      createRecipe()
    } else {
      updateRecipe()
    }
  }

  fun updateName(name: String) {
    _recipeState.update {
      it.copy(name = name)
    }
  }

  fun updateDescription(description: String) {
    _recipeState.update {
      it.copy(description = description)
    }
  }

  fun updateNotes(notes: String) {
    _recipeState.update {
      it.copy(notes = notes)
    }
  }

  fun updateServings(servings: Int) {
    _recipeState.update {
      it.copy(servings = servings)
    }
  }

  fun updatePrepTime(prepTime: Int) {
    _recipeState.update {
      it.copy(prepTime = prepTime)
    }
  }

  fun updateCookTime(cookTime: Int) {
    _recipeState.update {
      it.copy(cookTime = cookTime)
    }
  }

  fun updateSource(source: String) {
    _recipeState.update {
      it.copy(source = source)
    }
  }

  fun updateSourceName(sourceName: String) {
    _recipeState.update {
      it.copy(sourceName = sourceName)
    }
  }

  fun updateIngredient(ingredient: Ingredient) {
    _recipeState.update { state ->
      state.copy(
        ingredients = state.ingredients.update(ingredient) { it.id }
      )
    }
  }

  fun removeIngredient(ingredient: Ingredient) {
    _recipeState.update {
      it.copy(ingredients = it.ingredients - ingredient)
    }
  }

  fun updateInstruction(instruction: Instruction) {
    _recipeState.update { state ->
      state.copy(
        instructions = state.instructions.update(instruction) { it.id }
      )
    }
  }

  fun removeInstruction(instruction: Instruction) {
    _recipeState.update {
      it.copy(instructions = it.instructions - instruction)
    }
  }

  fun updateEquipment(equipment: Equipment) {
    _recipeState.update { state ->
      state.copy(
        equipment = state.equipment.update(equipment) { it.id }
      )
    }
  }

  fun removeEquipment(equipment: Equipment) {
    _recipeState.update {
      it.copy(equipment = it.equipment - equipment)
    }
  }

  fun showMessage(message: String) {
    _recipeState.update {
      it.copy(userMessage = message)
    }
  }

  fun userMessageShown() {
    _recipeState.update {
      it.copy(userMessage = null)
    }
  }

  private fun <T, R : Comparable<R>> List<T>.update(
    item: T,
    selector: (T) -> R,
  ) = if (any { selector(it) == selector(item) }) {
    map { i -> if (selector(i) == selector(item)) item else i }
  } else {
    this + item
  }

  private fun createRecipe() = viewModelScope.launch {
    createdId = with(_recipeState.value) {
      recipeRepository.createRecipe(
        name = name,
        description = description,
        notes = notes,
        servings = servings,
        prepTime = prepTime,
        cookTime = cookTime,
        source = source,
        sourceName = sourceName,
        ingredients = ingredients,
        instructions = instructions,
        equipment = equipment
      )
    }

    _recipeState.update {
      it.copy(isRecipeSaved = true)
    }
  }

  private fun updateRecipe() {
    check(recipeId != null) {
      "No recipe id to update"
    }

    viewModelScope.launch {
      with(_recipeState.value) {
        recipeRepository.updateRecipe(
          id = recipeId,
          name = name,
          description = description,
          notes = notes,
          servings = servings,
          prepTime = prepTime,
          cookTime = cookTime,
          source = source,
          sourceName = sourceName,
          ingredients = ingredients,
          instructions = instructions,
          equipment = equipment
        )
      }

      _recipeState.update {
        it.copy(isRecipeSaved = true)
      }
    }
  }

  private fun loadRecipe(id: String) {
    _uiState.update { UiState.Loading }
    viewModelScope.launch {
      recipeRepository.fetchRecipe(id).onSuccess { recipe ->
        _recipeState.update {
          RecipeState(
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
        }
      }

      _uiState.update { UiState.Loaded(Unit) }
    }
  }

  private fun checkForInvalidForm(): String? = with(_recipeState.value) {
    return when {
      name.isBlank() -> "Name cannot be blank"
      description.isBlank() -> "Description cannot be blank"
      ingredients.isEmpty() -> "At least one ingredient is required"
      instructions.isEmpty() -> "At least one instruction is required"
      equipment.isEmpty() -> "At least one equipment is required"
      servings <= 0 -> "Servings must be greater than 0"
      cookTime <= 0 -> "Cook time must be greater than 0"
      else -> null
    }
  }
}