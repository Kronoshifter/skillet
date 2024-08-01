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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kronos.skilletapp.model.Equipment
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.Instruction
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
    ) { data ->
      AddEditRecipeContent(
        name = data.name,
        description = data.description,
        notes = data.notes,
        servings = data.servings,
        prepTime = data.prepTime,
        cookTime = data.cookTime,
        source = data.source,
        sourceName = data.sourceName,
        ingredients = data.ingredients,
        instructions = data.instructions,
        equipment = data.equipment,
        onNameChanged = vm::updateName,
        onDescriptionChanged = vm::updateDescription,
        onIngredientChanged = vm::updateIngredient,
      )

      LaunchedEffect(data.isRecipeSaved) {
        if (data.isRecipeSaved) {
          onRecipeUpdate(vm.getRecipeId())
        }
      }

      data.userMessage?.let {
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

      IngredientsContent(onIngredientChanged, ingredients)

    }
  }
}

@Composable
private fun IngredientsContent(
  onIngredientChanged: (Ingredient) -> Unit,
  ingredients: List<Ingredient>,
) {
  val keyboard = LocalSoftwareKeyboardController.current
  
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
      IconButton(
        onClick = { ingredientInput = "" }
      ) {
        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
      }
    },
    keyboardOptions = KeyboardOptions(
      imeAction = ImeAction.Done
    ),
    keyboardActions = KeyboardActions(
      onDone = {
        onIngredientChanged(IngredientParser.parseIngredient(ingredientInput))
        keyboard?.hide()
        ingredientInput = ""
      }
    )
  )

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
          onClick = { editing = true },
        )
      } else {
        IngredientEdit(
          ingredient = ingredient.toString()
        )
      }
    }
  }
}

@Composable
private fun IngredientEdit(
  ingredient: String,
) {
  var ingredientInput by remember { mutableStateOf(ingredient) }
//  OutlinedTextField(value =, onValueChange =) //TODO: finish this
}