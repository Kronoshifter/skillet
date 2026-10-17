package com.kronos.skilletapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import com.kronos.skilletapp.data.RecipeRepository
import com.kronos.skilletapp.domain.scraping.ScrapeRecipe
import com.kronos.skilletapp.domain.validation.ValidateRecipe
import com.kronos.skilletapp.model.Equipment
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.Instruction
import com.kronos.skilletapp.model.Recipe
import com.kronos.skilletapp.model.RecipeSource
import com.kronos.skilletapp.model.RecipeTime
import com.kronos.skilletapp.navigation.Route
import com.kronos.skilletapp.parser.IngredientParser
import com.kronos.skilletapp.utils.move
import com.kronos.skilletapp.utils.update
import com.kronos.skilletapp.utils.upsert
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddEditRecipeViewModel(
  private val recipeRepository: RecipeRepository,
  private val validateRecipe: ValidateRecipe,
  private val scrapeRecipe: ScrapeRecipe,
  private val ingredientParser: IngredientParser,
  handle: SavedStateHandle,
) : ViewModel() {
  private val args = handle.toRoute<Route.AddEditRecipe>()
  private val recipeId = args.recipeId
  private val recipeUrl = args.url
  private lateinit var createdId: String

  private val _isInitializing = MutableStateFlow(false)
  val isInitializing: StateFlow<Boolean> = _isInitializing.asStateFlow()

  private val _recipeFormState = MutableStateFlow(RecipeFormState())
  private var originalFormState = RecipeFormState()
  val recipeFormState: StateFlow<RecipeFormState> = _recipeFormState.asStateFlow()
  val recipeFormFieldOps: RecipeFormFieldOps = RecipeFormFieldOpsImpl(_recipeFormState, ::checkForChanges)
  val recipeFormCollectionOps: RecipeFormCollectionOps = RecipeFormCollectionOpsImpl(_recipeFormState, ::checkForChanges)

  init {
    recipeId?.let { loadRecipe(it) } ?: recipeUrl?.let { scUrl(it) }
  }

  fun getRecipeId(): String {
    return recipeId ?: createdId
  }

  fun saveRecipe() {
    _recipeFormState.value
      .let { state ->
        validateRecipe(
          name = state.name,
          ingredients = state.ingredients,
          instructions = state.instructions,
          servings = state.servings,
          cookTime = state.cookTime,
        )
      }
      .onOk {
        if (recipeId == null) {
          createRecipe()
        } else {
          updateRecipe()
        }
        markSaveInProgress()
      }
      .onErr { error -> showUserMessage(error.message) }
  }

  fun showMessage(message: String) {
    recipeFormFieldOps.showMessage(message)
  }

  fun userMessageShown() {
    recipeFormFieldOps.userMessageShown()
  }

  private fun checkForChanges() {
    _recipeFormState.checkForChanges(originalFormState)
  }

  private fun loadState(newState: RecipeFormState) {
    originalFormState = newState
    _recipeFormState.update { newState }
  }

  private fun markSaveInProgress() {
    _recipeFormState.update { it.copy(isSaveInProgress = true) }
  }

  private fun markRecipeSaved() {
    _recipeFormState.update { it.copy(isRecipeSaved = true) }
  }

  private fun showUserMessage(message: String) {
    _recipeFormState.update { it.copy(userMessage = message) }
  }

  private fun buildRecipeFromState(): Recipe =
    with(_recipeFormState.value) {
      Recipe(
        name = name,
        description = description,
        cover = image,
        notes = notes,
        servings = servings,
        time = RecipeTime(prepTime, cookTime),
        source = RecipeSource(sourceName, source),
        ingredients = ingredients,
        instructions = instructions,
        equipment = equipment,
      )
    }

  private fun createRecipe() = viewModelScope.launch {
    val recipe = buildRecipeFromState()
    createdId = recipeRepository.createRecipe(recipe)

    markRecipeSaved()
  }

  private fun updateRecipe() {
    checkNotNull(recipeId) { "No recipe id to update" }

    viewModelScope.launch {
      val recipe = buildRecipeFromState()
      recipeRepository.updateRecipe(recipeId, recipe)

      markRecipeSaved()
    }
  }

  private fun loadRecipe(id: String) {
    _isInitializing.update { true }
    viewModelScope.launch {
      recipeRepository.fetchRecipe(id).let { recipe ->
        loadState(
          RecipeFormState(
            name = recipe.name,
            description = recipe.description,
            notes = recipe.notes,
            servings = recipe.servings,
            prepTime = recipe.time.preparation,
            cookTime = recipe.time.cooking,
            source = recipe.source.source,
            sourceName = recipe.source.name,
            image = recipe.cover,
            ingredients = recipe.ingredients,
            instructions = recipe.instructions,
            equipment = recipe.equipment,
          )
        )
      }

      _isInitializing.update { false }
    }
  }

  fun scUrl(url: String) {
    _isInitializing.update { true }
    viewModelScope.launch {
      scrapeRecipe(url)
        .onOk { scraped ->
          loadState(
            RecipeFormState(
              name = scraped.name,
              description = scraped.description.orEmpty(),
              servings = scraped.servings ?: 0,
              prepTime = scraped.prepTimeMinutes ?: 0,
              cookTime = scraped.cookTimeMinutes ?: 0,
              source = scraped.sourceUrl,
              sourceName = scraped.sourceName.orEmpty(),
              ingredients = scraped.ingredients.map { ingredientParser.parseIngredient(text = it) },
              instructions = scraped.instructions.map { Instruction(text = it) },
              tharBeChanges = true,
            )
          )
        }
        .onErr { error ->
          Log.e("Recipe Scraping", "Failed to scrape recipe: ${error.message}")
          showUserMessage("Recipe could not be imported, verify the link and try again, or enter the recipe manually")
        }

      _isInitializing.update { false }
    }
  }
}

/**
 * Compares [this]'s current state against [original] across the 12 content fields and flips [RecipeFormState.tharBeChanges]. Deliberately
 * excludes userMessage, isSaveInProgress, and isRecipeSaved — those are lifecycle flags, not user edits.
 */
private fun MutableStateFlow<RecipeFormState>.checkForChanges(original: RecipeFormState) {
  update { state ->
    state.copy(
      tharBeChanges =
        state.let {
          it.name != original.name ||
            it.description != original.description ||
            it.notes != original.notes ||
            it.servings != original.servings ||
            it.prepTime != original.prepTime ||
            it.cookTime != original.cookTime ||
            it.source != original.source ||
            it.sourceName != original.sourceName ||
            it.image != original.image ||
            it.ingredients != original.ingredients ||
            it.instructions != original.instructions ||
            it.equipment != original.equipment
        }
    )
  }
}

/**
 * Scalar field mutations for the add/edit form: each operation updates one field of [recipeFormState] and then reports the change through
 * [checkForChanges].
 */
private class RecipeFormFieldOpsImpl(
  private val recipeFormState: MutableStateFlow<RecipeFormState>,
  private val checkForChanges: () -> Unit,
) : RecipeFormFieldOps {
  override fun updateName(name: String) {
    recipeFormState.update { it.copy(name = name) }
    checkForChanges()
  }

  override fun updateDescription(description: String) {
    recipeFormState.update { it.copy(description = description) }
    checkForChanges()
  }

  override fun updateNotes(notes: String) {
    recipeFormState.update { it.copy(notes = notes) }
    checkForChanges()
  }

  override fun updateServings(servings: Int) {
    recipeFormState.update { it.copy(servings = servings) }
    checkForChanges()
  }

  override fun updatePrepTime(prepTime: Int) {
    recipeFormState.update { it.copy(prepTime = prepTime) }
    checkForChanges()
  }

  override fun updateCookTime(cookTime: Int) {
    recipeFormState.update { it.copy(cookTime = cookTime) }
    checkForChanges()
  }

  override fun updateSource(source: String) {
    recipeFormState.update { it.copy(source = source) }
    checkForChanges()
  }

  override fun updateSourceName(sourceName: String) {
    recipeFormState.update { it.copy(sourceName = sourceName) }
    checkForChanges()
  }

  override fun updateImage(image: String?) {
    recipeFormState.update { it.copy(image = image) }
    checkForChanges()
  }

  override fun showMessage(message: String) {
    recipeFormState.update { it.copy(userMessage = message) }
    checkForChanges()
  }

  override fun userMessageShown() {
    recipeFormState.update { it.copy(userMessage = null) }
    checkForChanges()
  }
}

/**
 * Collection mutations (update/remove/move) for the add/edit form's ingredients, instructions, and equipment: each operation rewrites the
 * affected list(s) of [recipeFormState] and then reports the change through [checkForChanges].
 */
private class RecipeFormCollectionOpsImpl(
  private val recipeFormState: MutableStateFlow<RecipeFormState>,
  private val checkForChanges: () -> Unit,
) : RecipeFormCollectionOps {
  override fun updateIngredient(ingredient: Ingredient) {
    recipeFormState.update { state ->
      state.copy(
        ingredients = state.ingredients.upsert(ingredient) { it.id },
        instructions =
          state.instructions.map { instruction -> instruction.copy(ingredients = instruction.ingredients.update(ingredient) { it.id }) },
      )
    }
    checkForChanges()
  }

  override fun removeIngredient(ingredient: Ingredient) {
    recipeFormState.update {
      it.copy(
        ingredients = it.ingredients - ingredient,
        instructions = it.instructions.map { instruction -> instruction.copy(ingredients = instruction.ingredients - ingredient) },
      )
    }
    checkForChanges()
  }

  override fun moveIngredient(from: Int, to: Int) {
    recipeFormState.update { it.copy(ingredients = it.ingredients.move(from, to)) }
    checkForChanges()
  }

  override fun updateInstruction(instruction: Instruction) {
    recipeFormState.update { state -> state.copy(instructions = state.instructions.upsert(instruction) { it.id }) }
    checkForChanges()
  }

  override fun removeInstruction(instruction: Instruction) {
    recipeFormState.update { it.copy(instructions = it.instructions - instruction) }
    checkForChanges()
  }

  override fun moveInstruction(from: Int, to: Int) {
    recipeFormState.update { it.copy(instructions = it.instructions.move(from, to)) }
    checkForChanges()
  }

  override fun updateEquipment(equipment: Equipment) {
    recipeFormState.update { state -> state.copy(equipment = state.equipment.upsert(equipment) { it.id }) }
    checkForChanges()
  }

  override fun removeEquipment(equipment: Equipment) {
    recipeFormState.update { it.copy(equipment = it.equipment - equipment) }
    checkForChanges()
  }

  override fun moveEquipment(from: Int, to: Int) {
    recipeFormState.update { it.copy(equipment = it.equipment.move(from, to)) }
    checkForChanges()
  }
}
