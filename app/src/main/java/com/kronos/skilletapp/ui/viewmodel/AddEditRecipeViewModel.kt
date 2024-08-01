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
import org.koin.core.KoinApplication.Companion.init

data class AddEditRecipeUiState(
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
  val isRecipeSaved: Boolean = false
)

class AddEditRecipeViewModel(
  private val recipeRepository: RecipeRepository,
  handle: SavedStateHandle,
) : ViewModel() {
  private val args = handle.toRoute<Route.AddEditRecipe>()
  private val recipeId = args.recipeId
  private lateinit var createdId: String

  private val _isLoading = MutableStateFlow(false)
  private val _addEditRecipeUiState: MutableStateFlow<AddEditRecipeUiState> = MutableStateFlow(AddEditRecipeUiState())
//  val uiState = _addEditRecipeUiState.asStateFlow()

  val uiState = combine(_isLoading, _addEditRecipeUiState) { loading, uiState ->
    when {
      loading -> UiState.Loading
      else -> UiState.Success(uiState)
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000L),
    initialValue = UiState.Success(AddEditRecipeUiState())
  )

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
      _addEditRecipeUiState.update {
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
    _addEditRecipeUiState.update {
      it.copy(name = name)
    }
  }

  fun updateDescription(description: String) {
    _addEditRecipeUiState.update {
      it.copy(description = description)
    }
  }

  fun updateNotes(notes: String) {
    _addEditRecipeUiState.update {
      it.copy(notes = notes)
    }
  }

  fun updateServings(servings: Int) {
    _addEditRecipeUiState.update {
      it.copy(servings = servings)
    }
  }

  fun updatePrepTime(prepTime: Int) {
    _addEditRecipeUiState.update {
      it.copy(prepTime = prepTime)
    }
  }

  fun updateCookTime(cookTime: Int) {
    _addEditRecipeUiState.update {
      it.copy(cookTime = cookTime)
    }
  }

  fun updateSource(source: String) {
    _addEditRecipeUiState.update {
      it.copy(source = source)
    }
  }

  fun updateSourceName(sourceName: String) {
    _addEditRecipeUiState.update {
      it.copy(sourceName = sourceName)
    }
  }

  fun updateIngredient(ingredient: Ingredient) {
    _addEditRecipeUiState.update { state ->
      state.copy(
        ingredients = state.ingredients.update(ingredient) { it.id }
      )
    }
  }

  fun removeIngredient(ingredient: Ingredient) {
    _addEditRecipeUiState.update {
      it.copy(ingredients = it.ingredients - ingredient)
    }
  }

  fun updateInstruction(instruction: Instruction) {
    _addEditRecipeUiState.update { state ->
      state.copy(
        instructions = state.instructions.update(instruction) { it.id }
      )
    }
  }

  fun removeInstruction(instruction: Instruction) {
    _addEditRecipeUiState.update {
      it.copy(instructions = it.instructions - instruction)
    }
  }

  fun updateEquipment(equipment: Equipment) {
    _addEditRecipeUiState.update { state ->
      state.copy(
        equipment = state.equipment.update(equipment) { it.id }
      )
    }
  }

  fun removeEquipment(equipment: Equipment) {
    _addEditRecipeUiState.update {
      it.copy(equipment = it.equipment - equipment)
    }
  }

  fun showMessage(message: String) {
    _addEditRecipeUiState.update {
      it.copy(userMessage = message)
    }
  }

  fun userMessageShown() {
    _addEditRecipeUiState.update {
      it.copy(userMessage = null)
    }
  }

  private fun <T, R : Comparable<R>> List<T>.update(
    item: T,
    selector: (T) -> R
  ) = if (any { selector(it) == selector(item) }) {
    map { i -> if (selector(i) == selector(item)) item else i }
  } else {
    this + item
  }

  private fun createRecipe() = viewModelScope.launch {
    createdId = with(_addEditRecipeUiState.value) {
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

    _addEditRecipeUiState.update {
      it.copy(isRecipeSaved = true)
    }
  }

  private fun updateRecipe() {
    check(recipeId != null) {
      "No recipe id to update"
    }

    viewModelScope.launch {
      with(_addEditRecipeUiState.value) {
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

      _addEditRecipeUiState.update {
        it.copy(isRecipeSaved = true)
      }
    }
  }

  private fun loadRecipe(id: String) {
    _isLoading.update { true }
    viewModelScope.launch {
      recipeRepository.fetchRecipe(id).onSuccess { recipe ->
          _addEditRecipeUiState.update {
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
          }
        }

      _isLoading.update { false }
    }
  }

  private fun checkForInvalidForm(): String? = with(_addEditRecipeUiState.value) {
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