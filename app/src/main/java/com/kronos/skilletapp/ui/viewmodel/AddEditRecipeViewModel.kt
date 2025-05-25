package com.kronos.skilletapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.mapBoth
import com.github.michaelbull.result.onFailure
import com.kronos.skilletapp.model.InvalidFormError
import com.kronos.skilletapp.data.RecipeRepository
import com.kronos.skilletapp.data.UiState
import com.kronos.skilletapp.utils.err
import com.kronos.skilletapp.utils.ok
import com.kronos.skilletapp.model.Equipment
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.Instruction
import com.kronos.skilletapp.navigation.Route
import com.kronos.skilletapp.parser.IngredientParser
import com.kronos.skilletapp.scraping.RecipeScrape
import com.kronos.skilletapp.scraping.RecipeScraper
import com.kronos.skilletapp.utils.move
import com.kronos.skilletapp.utils.update
import com.kronos.skilletapp.utils.upsert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration

data class RecipeState(
  val name: String = "",
  val description: String = "",
  val notes: String = "",
  val servings: Int = 0,
  val prepTime: Int = 0,
  val cookTime: Int = 0,
  val source: String = "",
  val sourceName: String = "",
  val image: String? = null,
  val ingredients: List<Ingredient> = emptyList(),
  val instructions: List<Instruction> = emptyList(),
  val equipment: List<Equipment> = emptyList(),
  val userMessage: String? = null,
  val isRecipeSaved: Boolean = false,
  val tharBeChanges: Boolean = false,
  val isSaveInProgress: Boolean = false,
)

class AddEditRecipeViewModel(
  private val recipeRepository: RecipeRepository,
  private val scraper: RecipeScraper,
  private val recipeParser: IngredientParser,
  handle: SavedStateHandle,
) : ViewModel() {
  private val args = handle.toRoute<Route.AddEditRecipe>()
  private val recipeId = args.recipeId
  private val recipeUrl = args.url
  private lateinit var createdId: String

  private val _uiState: MutableStateFlow<UiState<Nothing>> = MutableStateFlow(UiState.Loaded)
  private val _recipeState: MutableStateFlow<RecipeState> = MutableStateFlow(RecipeState())
  private var originalRecipeState = RecipeState()
  val uiState = _uiState.asStateFlow()
  val recipeState = _recipeState.asStateFlow()

  init {
    recipeId?.let {
      loadRecipe(it)
    } ?: recipeUrl?.let {
      scrapeRecipe(it)
    }
  }

  fun getRecipeId(): String {
    return recipeId ?: createdId
  }

  fun saveRecipe() {
    validateForm() onFailure { error ->
      _recipeState.update { it.copy(userMessage = error.message) }
      return
    }

    if (recipeId == null) {
      createRecipe()
    } else {
      updateRecipe()
    }

    _recipeState.update {
      it.copy(isSaveInProgress = true)
    }
  }

  fun updateName(name: String) {
    _recipeState.update {
      it.copy(name = name)
    }
    _recipeState.checkForChanges(originalRecipeState)
  }

  fun updateDescription(description: String) {
    _recipeState.update {
      it.copy(description = description)
    }
    _recipeState.checkForChanges(originalRecipeState)
  }

  fun updateNotes(notes: String) {
    _recipeState.update {
      it.copy(notes = notes)
    }
    _recipeState.checkForChanges(originalRecipeState)
  }

  fun updateServings(servings: Int) {
    _recipeState.update {
      it.copy(servings = servings)
    }
    _recipeState.checkForChanges(originalRecipeState)
  }

  fun updatePrepTime(prepTime: Int) {
    _recipeState.update {
      it.copy(prepTime = prepTime)
    }
    _recipeState.checkForChanges(originalRecipeState)
  }

  fun updateCookTime(cookTime: Int) {
    _recipeState.update {
      it.copy(cookTime = cookTime)
    }
    _recipeState.checkForChanges(originalRecipeState)
  }

  fun updateSource(source: String) {
    _recipeState.update {
      it.copy(source = source)
    }
    _recipeState.checkForChanges(originalRecipeState)
  }

  fun updateSourceName(sourceName: String) {
    _recipeState.update {
      it.copy(sourceName = sourceName)
    }
    _recipeState.checkForChanges(originalRecipeState)
  }

  fun updateImage(image: String?) {
    _recipeState.update {
      it.copy(image = image)
    }
    _recipeState.checkForChanges(originalRecipeState)
  }

  fun updateIngredient(ingredient: Ingredient) {
    _recipeState.update { state ->
      state.copy(
        ingredients = state.ingredients.upsert(ingredient) { it.id },
        instructions = state.instructions.map { instruction ->
          instruction.copy(ingredients = instruction.ingredients.update(ingredient) { it.id })
        }
      )
    }
    _recipeState.checkForChanges(originalRecipeState)
  }

  fun removeIngredient(ingredient: Ingredient) {
    _recipeState.update {
      it.copy(
        ingredients = it.ingredients - ingredient,
        instructions = it.instructions.map { instruction ->
          instruction.copy(ingredients = instruction.ingredients - ingredient)
        }
      )
    }
    _recipeState.checkForChanges(originalRecipeState)
  }

  fun moveIngredient(from: Int, to: Int) {
    _recipeState.update {
      it.copy(
        ingredients = it.ingredients.move(from, to),
      )
    }
    _recipeState.checkForChanges(originalRecipeState)
  }

  fun updateInstruction(instruction: Instruction) {
    _recipeState.update { state ->
      state.copy(
        instructions = state.instructions.upsert(instruction) { it.id }
      )
    }
    _recipeState.checkForChanges(originalRecipeState)
  }

  fun removeInstruction(instruction: Instruction) {
    _recipeState.update {
      it.copy(instructions = it.instructions - instruction)
    }
    _recipeState.checkForChanges(originalRecipeState)
  }

  fun moveInstruction(from: Int, to: Int) {
    _recipeState.update {
      it.copy(
        instructions = it.instructions.move(from, to),
      )
    }
    _recipeState.checkForChanges(originalRecipeState)
  }

  fun updateEquipment(equipment: Equipment) {
    _recipeState.update { state ->
      state.copy(
        equipment = state.equipment.upsert(equipment) { it.id }
      )
    }
    _recipeState.checkForChanges(originalRecipeState)
  }

  fun removeEquipment(equipment: Equipment) {
    _recipeState.update {
      it.copy(equipment = it.equipment - equipment)
    }
    _recipeState.checkForChanges(originalRecipeState)
  }

  fun moveEquipment(from: Int, to: Int) {
    _recipeState.update {
      it.copy(
        equipment = it.equipment.move(from, to),
      )
    }
    _recipeState.checkForChanges(originalRecipeState)
  }

  fun showMessage(message: String) {
    _recipeState.update {
      it.copy(userMessage = message)
    }
    _recipeState.checkForChanges(originalRecipeState)
  }

  fun userMessageShown() {
    _recipeState.update {
      it.copy(userMessage = null)
    }
    _recipeState.checkForChanges(originalRecipeState)
  }

  private fun MutableStateFlow<RecipeState>.checkForChanges(original: RecipeState) {
    update { state ->
      state.copy(
        tharBeChanges = state.let {
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

  private fun createRecipe() = viewModelScope.launch {
    createdId = _recipeState.value.run {
      recipeRepository.createRecipe(
        name = name,
        description = description,
        notes = notes,
        servings = servings,
        prepTime = prepTime,
        cookTime = cookTime,
        source = source,
        sourceName = sourceName,
        image = image,
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
      _recipeState.value.run {
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
          image = image,
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
      recipeRepository.fetchRecipe(id).let { recipe ->
        _recipeState.update {
          RecipeState(
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
            equipment = recipe.equipment
          ).also { originalRecipeState = it }
        }
      }

      _uiState.update { UiState.Loaded }
    }
  }

  fun scrapeRecipe(url: String) {
    _uiState.update { UiState.Loading }
    viewModelScope.launch(Dispatchers.IO) {
      _recipeState.update { state ->
        scraper.scrapeRecipe(url).mapBoth(
          success = { scrape ->
            RecipeState(
              name = scrape.recipe.name,
              description = scrape.recipe.description,
              servings = """\d+""".toRegex().let { regex ->
                regex.find(scrape.recipe.recipeYield.first { s -> regex.matches(s) })?.value?.toInt() ?: 0
              },
              prepTime = scrape.recipe.prepTime.parseMinutes(),
              cookTime = scrape.recipe.cookTime.parseMinutes(),
              source = url,
              sourceName = scrape.website?.name ?: """(\w+\.?)+\.\w+""".toRegex().find(url)?.value ?: "",
              ingredients = scrape.recipe.ingredients.map { recipeParser.parseIngredient(text = it) },
              instructions = scrape.recipe.instructions.map { Instruction(text = it.text) },
              tharBeChanges = true
            )
          },
          failure = {
            Log.e("Recipe Scraping", "Failed to scrape recipe: ${it.message}")
            state.copy(
              userMessage = "Recipe could not be imported, verify the link and try again, or enter the recipe manually",
            )
          }
        )
      }

      _uiState.update { UiState.Loaded }
    }
  }

  private fun validateForm(): Result<RecipeState, InvalidFormError> = _recipeState.value.run {
    return when {
      name.isBlank() -> InvalidFormError("Name cannot be blank").err()
      ingredients.isEmpty() -> InvalidFormError("At least one ingredient is required").err()
      instructions.isEmpty() -> InvalidFormError("At least one instruction is required").err()
      servings <= 0 -> InvalidFormError("Servings must be greater than 0").err()
      cookTime <= 0 -> InvalidFormError("Cook time must be greater than 0").err()
      else -> ok()
    }
  }

  private fun RecipeScrape.toRecipeState() = RecipeState(
    name = recipe.name,
    description = recipe.description,
//              servings = """\d+""".toRegex().find(it.recipe.recipeYield)?.value?.toInt() ?: 0,
    servings = """\d+""".toRegex().let { regex ->
      regex.find(recipe.recipeYield.first { s -> regex.matches(s) })?.value?.toInt() ?: 0
    },
    prepTime = recipe.prepTime.parseMinutes(),
    cookTime = recipe.prepTime.parseMinutes(),
    source = website?.url ?: "",
    sourceName = website?.name ?: "",
    ingredients = recipe.ingredients.map { recipeParser.parseIngredient(text = it) },
    instructions = recipe.instructions.map { Instruction(text = it.text) }
  )

  private fun String.parseMinutes() = Duration.parseIsoString(this).inWholeMinutes.toInt()
}