package com.kronos.skilletapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.github.michaelbull.result.runCatching
import com.github.michaelbull.result.unwrap
import com.kronos.skilletapp.data.RecipeRepository
import com.kronos.skilletapp.model.*
import com.kronos.skilletapp.parser.IngredientParser
import com.kronos.skilletapp.ui.LoadingContent
import com.kronos.skilletapp.ui.component.IngredientRow
import com.kronos.skilletapp.ui.component.ItemPill
import com.kronos.skilletapp.ui.theme.SkilletAppTheme
import com.kronos.skilletapp.ui.viewmodel.AddEditRecipeViewModel
import com.kronos.skilletapp.utils.toFraction
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.androidx.compose.getViewModel

private enum class AddEditRecipeContentTab {
  Info,
  Ingredients,
  Instructions,
  Equipment
}

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

      recipeState.userMessage?.let { message ->
        LaunchedEffect(snackbarHostState, vm, message) {
          snackbarHostState.showSnackbar(message)
          vm.userMessageShown()
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
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
  var tab by remember { mutableStateOf(AddEditRecipeContentTab.Info) }
  val pagerState = rememberPagerState { AddEditRecipeContentTab.entries.size }

  Surface(
    modifier = modifier,
    color = MaterialTheme.colorScheme.background
  ) {
    Column(
//      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier
        .fillMaxSize()
//        .padding(8.dp)
//        .verticalScroll(rememberScrollState())
    ) {
      PrimaryScrollableTabRow(
        selectedTabIndex = tab.ordinal,
        modifier = Modifier
          .fillMaxWidth(),
//        edgePadding = TabRowDefaults.ScrollableTabRowEdgeStartPadding / 2
      ) {
        Tab(
          selected = tab == AddEditRecipeContentTab.Info,
          onClick = { tab = AddEditRecipeContentTab.Info },
          text = { Text(text = "Info") }
        )

        Tab(
          selected = tab == AddEditRecipeContentTab.Ingredients,
          onClick = { tab = AddEditRecipeContentTab.Ingredients },
          text = { Text(text = "Ingredients") }
        )

        Tab(
          selected = tab == AddEditRecipeContentTab.Instructions,
          onClick = { tab = AddEditRecipeContentTab.Instructions },
          text = { Text(text = "Instructions") }
        )

        Tab(
          selected = tab == AddEditRecipeContentTab.Equipment,
          onClick = { tab = AddEditRecipeContentTab.Equipment },
          text = { Text(text = "Equipment") }
        )
      }

      LaunchedEffect(tab) {
        pagerState.animateScrollToPage(tab.ordinal)
      }

      LaunchedEffect(pagerState.targetPage) {
        tab = AddEditRecipeContentTab.entries[pagerState.targetPage]
      }

      HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxWidth()
      ) {
        val page = AddEditRecipeContentTab.entries[it]
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.TopCenter
        ) {
          when (page) {
            AddEditRecipeContentTab.Info -> RecipeInfoContent(
              name = name,
              description = description,
              onNameChanged = onNameChanged,
              onDescriptionChanged = onDescriptionChanged
            )

            AddEditRecipeContentTab.Ingredients -> IngredientsContent(
              ingredients = ingredients,
              onIngredientChanged = onIngredientChanged,
              onRemoveIngredient = onRemoveIngredient,
              onUserMessage = onUserMessage
            )

            AddEditRecipeContentTab.Instructions -> {}
            AddEditRecipeContentTab.Equipment -> {}
          }
        }
      }
    }
  }
}

@Composable
private fun RecipeInfoContent(
  name: String,
  description: String,
  onNameChanged: (String) -> Unit,
  onDescriptionChanged: (String) -> Unit,
) {
  val keyboard = LocalSoftwareKeyboardController.current

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(8.dp)
  ) {
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

    Spacer(modifier = Modifier.height(16.dp))

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
        capitalization = KeyboardCapitalization.Sentences,
        imeAction = ImeAction.Done
      ),
      keyboardActions = KeyboardActions(onDone = { keyboard?.hide() })
    )
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

  val state = rememberLazyListState()
  val scope = rememberCoroutineScope()

  LazyColumn(
    state = state,
    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
    modifier = Modifier
      .fillMaxWidth()
      .padding(8.dp)
//        .padding(start = 8.dp)
  ) {
    items(
      items = ingredients,
      key = { it.id },
      contentType = { "Ingredient" }
    ) { ingredient ->
      var editing by remember { mutableStateOf(false) }

      if (!editing) {
        IngredientRow(
          ingredient = ingredient,
          onClick = {
            editing = true
          },
        )
      } else {
        val focusRequester = remember { FocusRequester() }

        IngredientEdit(
          ingredient = ingredient,
          modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
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

    item {
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
                scope.launch { state.animateScrollToItem(ingredients.size) }
              }.onFailure {
                onUserMessage("Failed to parse ingredient: ${it.message}")
              }
            keyboard?.hide()
          }
        )
      )
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
  var ingredientInput by remember {
    mutableStateOf(
      TextFieldValue(
        text = ingredient.raw,
        selection = TextRange(ingredient.raw.length)
      )
    )
  }

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

@Composable
fun InstructionsContent(
  instructions: List<Instruction>,
  ingredients: List<Ingredient>,
  onInstructionChanged: (Instruction) -> Unit,
  onRemoveInstruction: (Instruction) -> Unit,
  onUserMessage: (String) -> Unit,
) {
  val keyboard = LocalSoftwareKeyboardController.current

  val state = rememberLazyListState()
  val scope = rememberCoroutineScope()

  LazyColumn(
    state = state,
    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
    modifier = Modifier
      .fillMaxWidth()
      .padding(8.dp)
  ) {
    itemsIndexed(
      items = instructions,
      key = { _, instruction -> instruction.id },
      contentType = { _, _ -> "Instruction" }
    ) { index, instruction ->
      Text(
        text = "Step ${index + 1}",
        fontWeight = FontWeight.Bold,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp)
      )

      InstructionComponent(
        instruction = instruction,
        ingredients = ingredients,
        onInstructionChanged = onInstructionChanged,
        onClick = {}
      )

      if (instruction != instructions.last()) {
        HorizontalDivider()
      }
    }

    item {
      var instructionInput by remember { mutableStateOf("") }

      OutlinedTextField(
        value = instructionInput,
        onValueChange = { instructionInput = it },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(text = "Add an instruction") },
        trailingIcon = {
          if (instructionInput.isNotBlank()) {
            IconButton(
              onClick = { instructionInput = "" }
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
            val instruction = Instruction(instructionInput)
            onInstructionChanged(instruction)
            instructionInput = ""
            scope.launch { state.animateScrollToItem(instructions.size) }
            keyboard?.hide()
          }
        )
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun InstructionComponent(
  instruction: Instruction,
  ingredients: List<Ingredient>,
  onInstructionChanged: (Instruction) -> Unit,
  onClick: () -> Unit,
) {
  var showBottomSheet by remember { mutableStateOf(false) }
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val scope = rememberCoroutineScope()

  Column(
    verticalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
  ) {
    Text(
      text = instruction.text,
      modifier = Modifier
    )

    if (instruction.ingredients.isNotEmpty()) {
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
          .fillMaxWidth()
      ) {
        instruction.ingredients.forEach { ingredient ->
          ItemPill(
            leadingContent = {
              val quantity = when (ingredient.measurement.unit.system) {
                MeasurementSystem.Metric -> ingredient.measurement.quantity.toString().take(4).removeSuffix(".")
                else -> ingredient.measurement.quantity.toFraction().roundToNearestFraction().reduce().toDisplayString()
              }

              Text(
                text = "$quantity ${ingredient.measurement.unit.abbreviation}",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 18.sp,
                modifier = Modifier.padding(8.dp)
              )
            },
            trailingIcon = {
              IconButton(
                onClick = {
                  onInstructionChanged(
                    instruction.copy(
                      ingredients = instruction.ingredients - ingredient
                    )
                  )
                }
              ) {
                Icon(imageVector = Icons.Default.Clear, contentDescription = "Edit")
              }
            }
          ) {
            Text(
              text = ingredient.name,
              modifier = Modifier
            )
          }
        }
      }
    }

    ItemPill(
      enabled = true,
      onClick = {
        showBottomSheet = true
      },
      leadingContent = {
        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(8.dp))
      }
    ) {
      Text(text = "Add Ingredient", modifier = Modifier.padding(end = 8.dp))
    }
  }

  if (showBottomSheet) {
    ModalBottomSheet(
      onDismissRequest = { showBottomSheet = false },
      sheetState = sheetState
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
        modifier = Modifier
          .fillMaxWidth()
          .padding(8.dp)
      ) {
        Text(
          text = "Select Ingredients",
          style = MaterialTheme.typography.titleLarge
        )

        val newIngredients = remember { instruction.ingredients.toMutableStateList() }

        LazyColumn(
          contentPadding = PaddingValues(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxSize()
        ) {
          items(ingredients) { ingredient ->
            ItemPill(
              modifier = Modifier.fillMaxWidth(),
              leadingContent = {
                val quantity = when (ingredient.measurement.unit.system) {
                  MeasurementSystem.Metric -> ingredient.measurement.quantity.toString().take(4).removeSuffix(".")
                  else -> ingredient.measurement.quantity.toFraction().roundToNearestFraction().reduce().toDisplayString()
                }

                Text(
                  text = "$quantity ${ingredient.measurement.unit.abbreviation}",
                  color = MaterialTheme.colorScheme.onPrimary,
                  fontSize = 18.sp,
                  modifier = Modifier.padding(8.dp)
                )
              },
              trailingIcon = {
                Checkbox(
                  checked = newIngredients.contains(ingredient),
                  onCheckedChange = null,
                )
              },
              enabled = true,
              onClick = {
                if (newIngredients.contains(ingredient)) {
                  newIngredients.remove(ingredient)
                } else {
                  newIngredients.add(ingredient)
                }
              }
            ) {
              Text(
                text = ingredient.name,
                modifier = Modifier
              )
            }
          }
        }

        Button(
          onClick = {
            onInstructionChanged(instruction.copy(ingredients = newIngredients))

            scope.launch { sheetState.hide() }.invokeOnCompletion {
              if (!sheetState.isVisible) {
                showBottomSheet = false
              }
            }
          }
        ) {
          Text(text = "Save")
        }
      }
    }
  }
}

@Preview
@Composable
fun AddEditRecipeContentPreview() {
  val repository = RecipeRepository()
  val recipe = runBlocking { repository.fetchRecipe("test") }.unwrap()

  SkilletAppTheme {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      AddEditRecipeContent(
        modifier = Modifier.fillMaxSize(),
        name = recipe.name,
        description = recipe.description,
        ingredients = recipe.ingredients,
        instructions = recipe.instructions,
        equipment = recipe.equipment,
        source = recipe.source.source,
        sourceName = recipe.source.name,
        servings = recipe.servings,
        prepTime = recipe.time.preparation,
        cookTime = recipe.time.cooking,
        notes = recipe.notes,
        onNameChanged = {},
        onDescriptionChanged = {},
        onIngredientChanged = {},
        onRemoveIngredient = {},
        onUserMessage = {},
        onInstructionChanged = {},
        onServingsChanged = {},
        onPrepTimeChanged = {},
        onCookTimeChanged = {},
        onNotesChanged = {},
        onSourceChanged = {},
        onSourceNameChanged = {},
        onRemoveInstruction = {},
        onRemoveEquipment = {},
        onEquipmentChanged = {}
      )
    }
  }
}

@Preview
@Composable
fun IngredientsTabPreview() {
  val repository = RecipeRepository()
  val recipe = runBlocking { repository.fetchRecipe("test") }.unwrap()

  val ingredients = recipe.ingredients.toMutableList()

  SkilletAppTheme {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      IngredientsContent(
        ingredients = ingredients,
        onIngredientChanged = {
          ingredients.add(it)
        },
        onRemoveIngredient = {
          ingredients.remove(it)
        },
        onUserMessage = {}
      )
    }
  }
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

@Preview
@Composable
fun InstructionsTabPreview() {
  val repository = RecipeRepository()
  val recipe = runBlocking { repository.fetchRecipe("test") }.unwrap()

  var instructions by remember { mutableStateOf(recipe.instructions) }
  val ingredients = recipe.ingredients

  SkilletAppTheme {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      InstructionsContent(
        instructions = instructions,
        ingredients = ingredients,
        onInstructionChanged = { instruction ->
          instructions = if (instructions.any { it.id == instruction.id }) {
            instructions.map { i ->
              if (i.id == instruction.id) instruction else i
            }
          } else {
            instructions + instruction
          }
        },
        onRemoveInstruction = {
          instructions = instructions - it
        },
        onUserMessage = {}
      )
    }
  }
}