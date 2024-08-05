package com.kronos.skilletapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.github.michaelbull.result.runCatching
import com.kronos.skilletapp.model.*
import com.kronos.skilletapp.parser.IngredientParser
import com.kronos.skilletapp.ui.LoadingContent
import com.kronos.skilletapp.ui.component.IngredientRow
import com.kronos.skilletapp.ui.viewmodel.AddEditRecipeViewModel
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRecipeScreen(
  title: String,
  onBack: () -> Unit,
  onRecipeUpdate: (String) -> Unit,
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
  vm: AddEditRecipeViewModel = getViewModel(),
) {
  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          TextButton(onClick = vm::saveRecipe) {
            Text(text = "Save")
          }
        }
      )
    },
    snackbarHost = {
      SnackbarHost(
        hostState = snackbarHostState
      )
    }
  ) { paddingValues ->
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    LoadingContent(
      state = uiState,
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
    ) {
      val recipeState by vm.recipeState.collectAsStateWithLifecycle()

      AddEditRecipeContent(
        name = recipeState.name,
        description = recipeState.description,
        notes = recipeState.notes,
        servings = recipeState.servings,
        prepTime = recipeState.prepTime,
        cookTime = recipeState.cookTime,
        source = recipeState.source,
        sourceName = recipeState.sourceName,
        ingredients = recipeState.ingredients,
        instructions = recipeState.instructions,
        equipment = recipeState.equipment,
        onNameChanged = vm::updateName,
        onDescriptionChanged = vm::updateDescription,
        onIngredientChanged = vm::updateIngredient,
        onRemoveIngredient = vm::removeIngredient,
        onUserMessage = vm::showMessage,
      )

      LaunchedEffect(recipeState.isRecipeSaved) {
        if (recipeState.isRecipeSaved) {
          onRecipeUpdate(vm.getRecipeId())
        }
      }

      recipeState.userMessage?.let {
        LaunchedEffect(snackbarHostState, vm, it) {
          snackbarHostState.showSnackbar(it)
          vm.userMessageShown()
        }
      }
    }
  }
}

@Composable
fun AddEditRecipeContent(
  name: String,
  description: String,
  notes: String,
  servings: Int,
  prepTime: Int,
  cookTime: Int,
  source: String,
  sourceName: String,
  ingredients: List<Ingredient>,
  instructions: List<Instruction>,
  equipment: List<Equipment>,
  onNameChanged: (String) -> Unit = {},
  onDescriptionChanged: (String) -> Unit = {},
  onNotesChanged: (String) -> Unit = {},
  onServingsChanged: (Int) -> Unit = {},
  onPrepTimeChanged: (Int) -> Unit = {},
  onCookTimeChanged: (Int) -> Unit = {},
  onSourceChanged: (String) -> Unit = {},
  onSourceNameChanged: (String) -> Unit = {},
  onIngredientChanged: (Ingredient) -> Unit = {},
  onRemoveIngredient: (Ingredient) -> Unit = {},
  onInstructionChanged: (Instruction) -> Unit = {},
  onRemoveInstruction: (Instruction) -> Unit = {},
  onEquipmentChanged: (Equipment) -> Unit = {},
  onRemoveEquipment: (Equipment) -> Unit = {},
  onUserMessage: (String) -> Unit = {},
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    color = MaterialTheme.colorScheme.background
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier
        .fillMaxSize()
        .padding(8.dp)
        .verticalScroll(rememberScrollState())
    ) {
      val keyboard = LocalSoftwareKeyboardController.current

      Text(
        text = "Title",
        style = MaterialTheme.typography.titleLarge
      )

      OutlinedTextField(
        value = name,
        onValueChange = onNameChanged,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(text = "Name") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
          capitalization = KeyboardCapitalization.Words,
          imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { keyboard?.hide() })
      )

      Text(
        text = "Description",
        style = MaterialTheme.typography.titleLarge
      )

      OutlinedTextField(
        value = description,
        onValueChange = onDescriptionChanged,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(text = "Description") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
          capitalization = KeyboardCapitalization.Words,
          imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { keyboard?.hide() })
      )

      IngredientsContent(
        ingredients = ingredients,
        onIngredientChanged = onIngredientChanged,
        onRemoveIngredient = onRemoveIngredient,
        onUserMessage = onUserMessage
      )

    }
  }
}

@Composable
private fun IngredientsContent(
  ingredients: List<Ingredient>,
  onIngredientChanged: (Ingredient) -> Unit,
  onRemoveIngredient: (Ingredient) -> Unit,
  onUserMessage: (String) -> Unit,
) {
  val keyboard = LocalSoftwareKeyboardController.current

  Column(
    verticalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Text(
      text = "Ingredients",
      style = MaterialTheme.typography.titleLarge
    )

    var ingredientInput by remember { mutableStateOf("") }

    OutlinedTextField(
      value = ingredientInput,
      onValueChange = { ingredientInput = it },
      modifier = Modifier.fillMaxWidth(),
      placeholder = { Text(text = "Add an ingredient") },
      trailingIcon = {
        if (ingredientInput.isNotBlank()) {
          IconButton(
            onClick = { ingredientInput = "" }
          ) {
            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
          }
        }
      },
      singleLine = true,
      keyboardOptions = KeyboardOptions(
        imeAction = ImeAction.Done
      ),
      keyboardActions = KeyboardActions(
        onDone = {
          runCatching { IngredientParser.parseIngredient(ingredientInput) }
            .onSuccess {
              onIngredientChanged(it)
              ingredientInput = ""
            }.onFailure {
              onUserMessage("Failed to parse ingredient: ${it.message}")
            }
          keyboard?.hide()
        }
      )
    )

    val focusRequester = remember { FocusRequester() }

    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 8.dp)
    ) {
      ingredients.forEach { ingredient ->
        var editing by remember { mutableStateOf(false) }

        if (!editing) {
          IngredientRow(
            ingredient = ingredient,
            onClick = {
              editing = true
            },
          )
        } else {
          IngredientEdit(
            ingredient = ingredient,
            modifier = Modifier
              .fillMaxWidth()
              .focusRequester(focusRequester),
//              .onFocusChanged {
//                if (editing &&!it.isFocused) {
//                  editing = false
//                }
//              },
            onEdit = {
              onIngredientChanged(it)
              editing = false
            },
            onRemove = {
              onRemoveIngredient(it)
              editing = false
            }
          )

          LaunchedEffect(editing) {
            if (editing) {
              focusRequester.requestFocus()
              focusRequester.captureFocus()
            } else {
              focusRequester.freeFocus()
            }
          }
        }
      }
    }
  }
}

@Composable
private fun IngredientEdit(
  ingredient: Ingredient,
  modifier: Modifier = Modifier,
  onEdit: (Ingredient) -> Unit = {},
  onRemove: (Ingredient) -> Unit = {},
) {
  var ingredientInput by remember { mutableStateOf(TextFieldValue(ingredient.raw, TextRange(ingredient.raw.length))) }

  OutlinedTextField(
    value = ingredientInput,
    onValueChange = { ingredientInput = it },
    modifier = modifier,
    singleLine = true,
    trailingIcon = {
      IconButton(
        onClick = {
          ingredientInput = ingredientInput.copy(text = "")
          onRemove(ingredient)
        }
      ) {
        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
      }
    },
    keyboardOptions = KeyboardOptions(
      imeAction = ImeAction.Done
    ),
    keyboardActions = KeyboardActions(
      onDone = {
        val newIngredient = IngredientParser.parseIngredient(ingredientInput.text)
        onEdit(newIngredient.copy(id = ingredient.id))
        ingredientInput = ingredientInput.copy(text = "")
      }
    )
  )
}

@Preview
@Composable
fun IngredientEditPreview() {
  val ingredients = mutableListOf(Ingredient("test", Measurement(1.0, MeasurementUnit.Cup), "1 cup test"))
  ingredients.add(Ingredient("test", Measurement(1.0, MeasurementUnit.Cup), "1 cup test"))

  Surface {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      ingredients.forEach { ing ->
        var editing by remember { mutableStateOf(false) }

        if (!editing) {
          IngredientRow(
            ingredient = ing,
            onClick = { editing = true },
          )
        } else {
          IngredientEdit(
            ingredient = ing,
            onRemove = { ingredients.remove(ing); editing = false },
            onEdit = { ingredient ->
              ingredients[ingredients.indexOfFirst { it.id == ing.id }] = ingredient; editing = false
            }
          )
        }
      }
    }
  }
}