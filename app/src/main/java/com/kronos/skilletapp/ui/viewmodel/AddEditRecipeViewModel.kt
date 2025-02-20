package com.kronos.skilletapp.ui.viewmodel

import android.R.attr.description
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.github.michaelbull.result.mapBoth
import com.kronos.skilletapp.Route
import com.kronos.skilletapp.data.RecipeRepository
import com.kronos.skilletapp.data.UiState
import com.kronos.skilletapp.model.*
import com.kronos.skilletapp.parser.IngredientParser
import com.kronos.skilletapp.scraping.RecipeHtml
import com.kronos.skilletapp.scraping.RecipeScrape
import com.kronos.skilletapp.scraping.RecipeScraper
import com.kronos.skilletapp.utils.move
import com.kronos.skilletapp.utils.update
import com.kronos.skilletapp.utils.upsert
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.stringtemplate.v4.compiler.Bytecode.instructions
import kotlin.collections.map
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

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

    _recipeState.update {
      it.copy(isSaveInProgress = true)
    }
  }

  fun updateName(name: String) {
    _recipeState.update {
      it.copy(name = name)
    }
    checkForChanges()
  }

  fun updateDescription(description: String) {
    _recipeState.update {
      it.copy(description = description)
    }
    checkForChanges()
  }

  fun updateNotes(notes: String) {
    _recipeState.update {
      it.copy(notes = notes)
    }
    checkForChanges()
  }

  fun updateServings(servings: Int) {
    _recipeState.update {
      it.copy(servings = servings)
    }
    checkForChanges()
  }

  fun updatePrepTime(prepTime: Int) {
    _recipeState.update {
      it.copy(prepTime = prepTime)
    }
    checkForChanges()
  }

  fun updateCookTime(cookTime: Int) {
    _recipeState.update {
      it.copy(cookTime = cookTime)
    }
    checkForChanges()
  }

  fun updateSource(source: String) {
    _recipeState.update {
      it.copy(source = source)
    }
    checkForChanges()
  }

  fun updateSourceName(sourceName: String) {
    _recipeState.update {
      it.copy(sourceName = sourceName)
    }
    checkForChanges()
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
    checkForChanges()
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
    checkForChanges()
  }

  fun moveIngredient(from: Int, to: Int) {
    _recipeState.update {
      it.copy(
        ingredients = it.ingredients.move(from, to),
      )
    }
    checkForChanges()
  }

  fun updateInstruction(instruction: Instruction) {
    _recipeState.update { state ->
      state.copy(
        instructions = state.instructions.upsert(instruction) { it.id }
      )
    }
    checkForChanges()
  }

  fun removeInstruction(instruction: Instruction) {
    _recipeState.update {
      it.copy(instructions = it.instructions - instruction)
    }
    checkForChanges()
  }

  fun moveInstruction(from: Int, to: Int) {
    _recipeState.update {
      it.copy(
        instructions = it.instructions.move(from, to),
      )
    }
    checkForChanges()
  }

  fun updateEquipment(equipment: Equipment) {
    _recipeState.update { state ->
      state.copy(
        equipment = state.equipment.upsert(equipment) { it.id }
      )
    }
    checkForChanges()
  }

  fun removeEquipment(equipment: Equipment) {
    _recipeState.update {
      it.copy(equipment = it.equipment - equipment)
    }
    checkForChanges()
  }

  fun moveEquipment(from: Int, to: Int) {
    _recipeState.update {
      it.copy(
        equipment = it.equipment.move(from, to),
      )
    }
    checkForChanges()
  }

  fun showMessage(message: String) {
    _recipeState.update {
      it.copy(userMessage = message)
    }
    checkForChanges()
  }

  fun userMessageShown() {
    _recipeState.update {
      it.copy(userMessage = null)
    }
    checkForChanges()
  }

  private fun checkForChanges() {
    _recipeState.update { state ->
      state.copy(
        tharBeChanges = state.let {
          it.name != originalRecipeState.name ||
              it.description != originalRecipeState.description ||
              it.notes != originalRecipeState.notes ||
              it.servings != originalRecipeState.servings ||
              it.prepTime != originalRecipeState.prepTime ||
              it.cookTime != originalRecipeState.cookTime ||
              it.source != originalRecipeState.source ||
              it.sourceName != originalRecipeState.sourceName ||
              it.ingredients != originalRecipeState.ingredients ||
              it.instructions != originalRecipeState.instructions ||
              it.equipment != originalRecipeState.equipment
        }
      )
    }
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
    viewModelScope.launch {
      _recipeState.update { state ->
        scraper.scrapeRecipe(url).mapBoth(
          success = {
            RecipeState(
              name = it.recipe.name,
              description = it.recipe.description,
              servings = """\d+""".toRegex().find(it.recipe.recipeYield)?.value?.toInt() ?: 0,
              prepTime = it.recipe.prepTime.parseMinutes(),
              cookTime = it.recipe.prepTime.parseMinutes(),
              source = it.website?.url ?: url,
              sourceName = it.website?.name ?: """(\w+\.?)+\.\w+""".toRegex().find(url)?.value ?: "",
              ingredients = it.recipe.ingredients.map { recipeParser.parseIngredient(text = it) },
              instructions = it.recipe.instructions.map { Instruction(text = it.text) }
            )
          },
          failure = {
            state.copy(
              userMessage = "Recipe could not be imported, verify the link and try again, or enter the recipe manually",
            )
          }
        )
      }

      _uiState.update { UiState.Loaded }
    }
  }

  private fun checkForInvalidForm(): String? = with(_recipeState.value) {
    return when {
      name.isBlank() -> "Name cannot be blank"
      ingredients.isEmpty() -> "At least one ingredient is required"
      instructions.isEmpty() -> "At least one instruction is required"
      servings <= 0 -> "Servings must be greater than 0"
      cookTime <= 0 -> "Cook time must be greater than 0"
      else -> null
    }
  }

  private fun RecipeScrape.toRecipeState() = RecipeState(
    name = recipe.name,
    description = recipe.description,
    servings = """\d+""".toRegex().find(recipe.recipeYield)?.value?.toInt() ?: 0,
    prepTime = recipe.prepTime.parseMinutes(),
    cookTime = recipe.prepTime.parseMinutes(),
    source = website?.url ?: "",
    sourceName = website?.name ?: "",
    ingredients = recipe.ingredients.map { recipeParser.parseIngredient(text = it) },
    instructions = recipe.instructions.map { Instruction(text = it.text) }
  )

  private fun String.parseMinutes() = Duration.parseIsoString(this).inWholeMinutes.toInt()
}