package com.kronos.skilletapp.ui.screen

import android.view.HapticFeedbackConstants
import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.github.michaelbull.result.runCatching
import com.kronos.skilletapp.model.*
import com.kronos.skilletapp.model.measurement.Measurement
import com.kronos.skilletapp.model.measurement.MeasurementUnit
import com.kronos.skilletapp.parser.IngredientParser
import com.kronos.skilletapp.ui.DisableRipple
import com.kronos.skilletapp.ui.KoinPreview
import com.kronos.skilletapp.ui.LoadingContent
import com.kronos.skilletapp.ui.component.*
import com.kronos.skilletapp.ui.dismiss
import com.kronos.skilletapp.ui.theme.SkilletAppTheme
import com.kronos.skilletapp.ui.viewmodel.AddEditRecipeViewModel
import com.kronos.skilletapp.utils.modifier.applyIf
import com.kronos.skilletapp.utils.move
import com.kronos.skilletapp.utils.pluralize
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

private enum class AddEditRecipeContentTab {
  Info,
  Ingredients,
  Instructions,
//  Equipment
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRecipeScreen(
  title: String,
  onBack: () -> Unit,
  onRecipeUpdate: (String) -> Unit,
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
  vm: AddEditRecipeViewModel = koinViewModel(),
) {
  var showDiscardChangesDialog by remember { mutableStateOf(false) }

  val recipeState by vm.recipeState.collectAsStateWithLifecycle()

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
          IconButton(
            onClick = {
              if (recipeState.tharBeChanges) {
                showDiscardChangesDialog = true
              } else {
                onBack()
              }
            }
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(onClick = vm::saveRecipe, enabled = !recipeState.isSaveInProgress) {
            Icon(imageVector = Icons.Default.Save, contentDescription = "Save")
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
        onServingsChanged = vm::updateServings,
        onPrepTimeChanged = vm::updatePrepTime,
        onCookTimeChanged = vm::updateCookTime,
        onSourceChanged = vm::updateSource,
        onSourceNameChanged = vm::updateSourceName,
        onNotesChanged = vm::updateNotes,
        onIngredientChanged = vm::updateIngredient,
        onRemoveIngredient = vm::removeIngredient,
        onMoveIngredient = vm::moveIngredient,
        onInstructionChanged = vm::updateInstruction,
        onRemoveInstruction = vm::removeInstruction,
        onMoveInstruction = vm::moveInstruction,
        onEquipmentChanged = vm::updateEquipment,
        onRemoveEquipment = vm::removeEquipment,
        onMoveEquipment = vm::moveEquipment,
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

  if (showDiscardChangesDialog) {
    ConfirmDialog(
      onDismissRequest = { showDiscardChangesDialog = false },
      onConfirm = {
        showDiscardChangesDialog = false
        onBack()
      },
      onDismiss = { showDiscardChangesDialog = false },
      title = "Discard Changes?",
      text = "Unsaved changes will be lost",
    )
  }

  if (recipeState.isSaveInProgress) {
    DisableRipple {
      Surface(
        modifier = Modifier.fillMaxSize(),
        color = BottomSheetDefaults.ScrimColor,
        onClick = {}
      ) {
        CircularProgressIndicator(
          modifier = Modifier.wrapContentSize()
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
  onNameChanged: (String) -> Unit,
  onDescriptionChanged: (String) -> Unit,
  onNotesChanged: (String) -> Unit,
  onServingsChanged: (Int) -> Unit,
  onPrepTimeChanged: (Int) -> Unit,
  onCookTimeChanged: (Int) -> Unit,
  onSourceChanged: (String) -> Unit,
  onSourceNameChanged: (String) -> Unit,
  onIngredientChanged: (Ingredient) -> Unit,
  onRemoveIngredient: (Ingredient) -> Unit,
  onMoveIngredient: (Int, Int) -> Unit,
  onInstructionChanged: (Instruction) -> Unit,
  onRemoveInstruction: (Instruction) -> Unit,
  onMoveInstruction: (Int, Int) -> Unit,
  onEquipmentChanged: (Equipment) -> Unit,
  onRemoveEquipment: (Equipment) -> Unit,
  onMoveEquipment: (Int, Int) -> Unit,
  onUserMessage: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  var tab by remember { mutableStateOf(AddEditRecipeContentTab.Info) }
  val pagerState = rememberPagerState { AddEditRecipeContentTab.entries.size }

  Box(
    modifier = modifier,
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
    ) {
      PrimaryScrollableTabRow(
        selectedTabIndex = tab.ordinal,
        modifier = Modifier
          .fillMaxWidth(),
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

//        Tab(
//          selected = tab == AddEditRecipeContentTab.Equipment,
//          onClick = { tab = AddEditRecipeContentTab.Equipment },
//          text = { Text(text = "Equipment") }
//        )
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
              source = source,
              sourceName = sourceName,
              description = description,
              servings = servings,
              prepTime = prepTime,
              cookTime = cookTime,
              notes = notes,
              onNameChanged = onNameChanged,
              onSourceChanged = onSourceChanged,
              onSourceNameChanged = onSourceNameChanged,
              onDescriptionChanged = onDescriptionChanged,
              onServingsChanged = onServingsChanged,
              onPrepTimeChanged = onPrepTimeChanged,
              onCookTimeChanged = onCookTimeChanged,
              onNotesChanged = onNotesChanged
            )

            AddEditRecipeContentTab.Ingredients -> IngredientsContent(
              ingredients = ingredients,
              onIngredientChanged = onIngredientChanged,
              onRemoveIngredient = onRemoveIngredient,
              onMoveIngredient = onMoveIngredient,
              onUserMessage = onUserMessage
            )

            AddEditRecipeContentTab.Instructions -> InstructionsContent(
              instructions = instructions,
              ingredients = ingredients,
              onInstructionChanged = onInstructionChanged,
              onRemoveInstruction = onRemoveInstruction,
              onMoveInstruction = onMoveInstruction,
              onUserMessage = onUserMessage
            )

//            AddEditRecipeContentTab.Equipment -> {}
          }
        }
      }
    }
  }
}

@ExperimentalMaterial3Api
@Composable
private fun RecipeInfoContent(
  name: String,
  source: String,
  sourceName: String,
  description: String,
  servings: Int,
  prepTime: Int,
  cookTime: Int,
  notes: String,
  onNameChanged: (String) -> Unit,
  onSourceChanged: (String) -> Unit,
  onSourceNameChanged: (String) -> Unit,
  onDescriptionChanged: (String) -> Unit,
  onServingsChanged: (Int) -> Unit,
  onPrepTimeChanged: (Int) -> Unit,
  onCookTimeChanged: (Int) -> Unit,
  onNotesChanged: (String) -> Unit
) {
  val keyboard = LocalSoftwareKeyboardController.current

  Column(
    verticalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier
      .fillMaxSize()
      .padding(8.dp)
      .verticalScroll(rememberScrollState())
  ) {
    Text(
      text = "Title",
      style = MaterialTheme.typography.titleLarge
    )

    OutlinedTextField(
      value = name,
      onValueChange = onNameChanged,
      placeholder = { Text(text = "The name of your recipe") },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true,
      keyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.Words,
        imeAction = ImeAction.Done
      ),
      keyboardActions = KeyboardActions(onDone = { keyboard?.hide() })
    )

    HorizontalDivider()

    Column {
      Text(
        text = "Source",
        style = MaterialTheme.typography.titleLarge
      )

      Text(
        text = "Where did you find this recipe?"
      )

      var showSourceSheet by remember { mutableStateOf(false) }
      val sheetState = rememberModalBottomSheetState()
      val scope = rememberCoroutineScope()

      TextButton(
        onClick = { showSourceSheet = true }
      ) {
        Text(
          text = sourceName.ifBlank { "Add Source" },
          style = MaterialTheme.typography.titleMedium
        )
      }

      if (source.isNotBlank()) {
        Text(
          text = source,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.secondary,
          modifier = Modifier.padding(start = 12.dp)
        )
      }

      if (showSourceSheet) {
        var sourceNameInput by remember { mutableStateOf(sourceName) }
        var sourceInput by remember { mutableStateOf(source) }

        ActionBottomSheet(
          onDismissRequest = { showSourceSheet = false },
          sheetState = sheetState,
          modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
          title = { Text(text = "Source") },
          action = {
            TextButton(
              onClick = {
                onSourceChanged(sourceInput)
                onSourceNameChanged(sourceNameInput)

                sheetState.dismiss(scope) { showSourceSheet = false }
              },
            ) {
              Text(text = "Save")
            }
          }
        ) {
          val focusManager = LocalFocusManager.current
          val sheetKeyboard = LocalSoftwareKeyboardController.current

          OutlinedTextField(
            value = sourceNameInput,
            onValueChange = { sourceNameInput = it },
            label = { Text("Name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
              imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
              onNext = { focusManager.moveFocus(FocusDirection.Next) }
            ),
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = sourceInput,
            onValueChange = { sourceInput = it },
            label = { Text("Source") },
            placeholder = { Text("Website URL, recipe book and page number...") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
              imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
              onDone = {
                sheetKeyboard?.hide()
                focusManager.clearFocus()
              }
            ),
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }

    HorizontalDivider()

//    Text(
//      text = "Description",
//      style = MaterialTheme.typography.titleLarge
//    )
//
//    OutlinedTextField(
//      value = description,
//      onValueChange = onDescriptionChanged,
//      modifier = Modifier.fillMaxWidth(),
//      minLines = 3,
//      keyboardOptions = KeyboardOptions(
//        capitalization = KeyboardCapitalization.Sentences,
//        imeAction = ImeAction.Done
//      ),
//      keyboardActions = KeyboardActions(onDone = { keyboard?.hide() })
//    )
//
//    HorizontalDivider()

    Column {
      Text(
        text = "Servings",
        style = MaterialTheme.typography.titleLarge
      )

      Text(
        text = "How many servings does this recipe make? This is used to scale the recipe."
      )

      var showServingsPicker by remember { mutableStateOf(false) }
      var servingsSelect by remember { mutableIntStateOf(servings) }
      val sheetState = rememberModalBottomSheetState()
      val scope = rememberCoroutineScope()

      TextButton(
        onClick = {
          showServingsPicker = true
          servingsSelect = servings
        }) {
        Text(
          text = servings.let { n -> if (n > 0) "$n serving".pluralize(n) { "${it}s" } else "Set servings" },
          style = MaterialTheme.typography.titleMedium
        )
      }

      if (showServingsPicker) {
        ActionBottomSheet(
          sheetState = sheetState,
          onDismissRequest = { showServingsPicker = false },
          modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
          title = { Text(text = "Servings") },
          action = {
            TextButton(
              onClick = {
                onServingsChanged(servingsSelect)
                sheetState.dismiss(scope) { showServingsPicker = false }
              },
            ) {
              Text(text = "Save")
            }
          }
        ) {
          InfiniteScrollingPicker(
            options = (0..99).toList(),
            selected = servingsSelect,
            onSelect = { servingsSelect = it },
          ) {
            Text(text = if (it != 0) it.toString() else "-")
          }
        }
      }
    }

    HorizontalDivider()

    Column {
      Text(
        text = "Prep Time",
        style = MaterialTheme.typography.titleLarge
      )

      Text(
        text = "How long does this recipe take to prepare?"
      )

      var showTimePicker by remember { mutableStateOf(false) }
      val sheetState = rememberModalBottomSheetState()
      val scope = rememberCoroutineScope()

      TextButton(onClick = { showTimePicker = true }) {
        val hours = prepTime / 60
        val minutes = prepTime % 60
        val text = when {
          hours > 0 && minutes > 0 -> "$hours ${"hour".pluralize(hours) { "${it}s" }}, $minutes ${
            "minute".pluralize(
              minutes
            ) { "${it}s" }
          }"

          hours > 0 -> "$hours hour".pluralize(hours) { "${it}s" }
          minutes > 0 -> "$minutes minute".pluralize(minutes) { "${it}s" }
          else -> "Set prep time"
        }

        Text(
          text = text,
          style = MaterialTheme.typography.titleMedium
        )
      }

      if (showTimePicker) {
        TimeSelectBottomSheet(
          initialTime = prepTime,
          sheetState = sheetState,
          onDismissRequest = { showTimePicker = false },
          title = { Text("Prep Time") },
          onTimeSelect = {
            onPrepTimeChanged(it)
            sheetState.dismiss(scope) { showTimePicker = false }
          }
        )
      }
    }

    HorizontalDivider()

    Column {
      Text(
        text = "Cook Time",
        style = MaterialTheme.typography.titleLarge
      )

      Text(
        text = "How long does this recipe take to cook?"
      )

      var showTimePicker by remember { mutableStateOf(false) }
      val sheetState = rememberModalBottomSheetState()
      val scope = rememberCoroutineScope()

      TextButton(onClick = { showTimePicker = true }) {
        val hours = cookTime / 60
        val minutes = cookTime % 60
        val text = when {
          hours > 0 && minutes > 0 -> "$hours ${"hour".pluralize(hours) { "${it}s" }}, $minutes ${
            "minute".pluralize(
              minutes
            ) { "${it}s" }
          }"

          hours > 0 -> "$hours hour".pluralize(hours) { "${it}s" }
          minutes > 0 -> "$minutes minute".pluralize(minutes) { "${it}s" }
          else -> "Set cook time"
        }

        Text(
          text = text,
          style = MaterialTheme.typography.titleMedium
        )
      }

      if (showTimePicker) {
        TimeSelectBottomSheet(
          initialTime = cookTime,
          sheetState = sheetState,
          onDismissRequest = { showTimePicker = false },
          title = { Text("Cook Time") },
          onTimeSelect = {
            onCookTimeChanged(it)

            sheetState.dismiss(scope) { showTimePicker = false }
          }
        )
      }
    }

    HorizontalDivider()

    Text(
      text = "Additional Notes",
      style = MaterialTheme.typography.titleLarge
    )

    OutlinedTextField(
      value = notes,
      onValueChange = onNotesChanged,
      modifier = Modifier.fillMaxWidth(),
      placeholder = { Text("Any additional notes about the recipe") },
      minLines = 3,
      keyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.Sentences,
        imeAction = ImeAction.Done
      ),
      keyboardActions = KeyboardActions(onDone = { keyboard?.hide() })
    )
  }
}

@ExperimentalFoundationApi
@Composable
private fun IngredientsContent(
  ingredients: List<Ingredient>,
  onIngredientChanged: (Ingredient) -> Unit,
  onRemoveIngredient: (Ingredient) -> Unit,
  onMoveIngredient: (Int, Int) -> Unit,
  onUserMessage: (String) -> Unit,
  parser: IngredientParser = koinInject()
) {
  val keyboard = LocalSoftwareKeyboardController.current
  val view = LocalView.current

  val scope = rememberCoroutineScope()
  val lazyListState = rememberLazyListState()
  val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
    onMoveIngredient(from.index, to.index)
  }

  var reordering by remember { mutableStateOf(false) }

  Column {
    TextButton(
      onClick = { reordering = !reordering },
      enabled = ingredients.size > 1,
    ) {
      Text(
        text = if (!reordering) "Reorder ingredients" else "Stop reordering",
        style = MaterialTheme.typography.titleMedium
      )
    }

    LazyColumn(
      state = lazyListState,
      verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight()
        .padding(8.dp)
    ) {
      items(
        items = ingredients,
        key = { it.id },
        contentType = { "Ingredient" }
      ) { ingredient ->
        ReorderableItem(
          state = reorderableLazyListState,
          key = ingredient.id
        ) { isDragging ->
          var editing by remember { mutableStateOf(false) }
          val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp, label = "Drag and Drop elevation")

          Surface(
            shadowElevation = elevation,
            shape = MaterialTheme.shapes.medium
          ) {
            AnimatedContent(
              targetState = editing,
              label = "Ingredient editing transition",
              transitionSpec = { fadeIn() togetherWith fadeOut() }
            ) { isEditing ->
              if (!isEditing) {
                IngredientRow(
                  ingredient = ingredient,
                  onClick = {
                    editing = true
                  },
                  trailingIcon = {
                    AnimatedVisibility(
                      visible = reordering,
                      enter = fadeIn() + expandHorizontally() + slideInHorizontally { it },
                      exit = fadeOut() + shrinkHorizontally() + slideOutHorizontally { it }
                    ) {
                      IconButton(
                        onClick = {},
                        modifier = Modifier.draggableHandle(
                          onDragStarted = { view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START) },
                          onDragStopped = { view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END) }
                        ),
                      ) {
                        Icon(imageVector = Icons.Default.DragHandle, contentDescription = "drag ingredient")
                      }
                    }
                  }
                )
              } else {
                val focusRequester = remember { FocusRequester() }
                val focusManager = LocalFocusManager.current
                var focusGained by remember { mutableStateOf(false) }

                IngredientEdit(
                  ingredient = ingredient,
                  modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                      if (focusGained) {
                        if (!it.isFocused || !it.hasFocus) {
                          editing = false
                        }
                      } else {
                        focusGained = it.isFocused || it.hasFocus
                      }
                    }
                    .focusRequester(focusRequester),
                  onEdit = {
                    onIngredientChanged(it)
                    editing = false
                  },
                  onRemove = {
                    onRemoveIngredient(it)
                    editing = false
                  },
                  onUserMessage = onUserMessage
                )

                LaunchedEffect(editing) {
                  if (editing) {
                    focusRequester.requestFocus()
                  } else {
                    focusManager.clearFocus()
                  }
                }
              }
            }
          }
        }
      }

      if (!reordering) {
        item(key = "Ingredient Input") {
          var ingredientInput by remember { mutableStateOf("") }

          //TODO: find some sort of onPaste callback
          OutlinedTextField(
            value = ingredientInput,
            onValueChange = { ingredientInput = it },
            modifier = Modifier
              .fillMaxWidth()
              .animateItem(),
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
                // TODO: parse multiple ingredients when a list is pasted in
                if (ingredientInput.isNotBlank()) {
                  if (ingredientInput.contains("\n")) {
                    runCatching { parser.parseIngredients(ingredientInput) }
                      .onSuccess { newIngredients ->
                        newIngredients.forEach { onIngredientChanged(it) }
                        ingredientInput = ""
                        scope.launch { lazyListState.animateScrollToItem(ingredients.size) }
                      }
                      .onFailure {
                        onUserMessage("Failed to parse ingredients: ${it.message}")
                      }
                  } else {
                    runCatching { parser.parseIngredient(ingredientInput) }
                      .onSuccess {
                        onIngredientChanged(it)
                        ingredientInput = ""
                        scope.launch { lazyListState.animateScrollToItem(ingredients.size) }
                      }.onFailure {
                        onUserMessage("Failed to parse ingredient: ${it.message}")
                      }
                  }
                }
                keyboard?.hide()
              }
            )
          )
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
  onUserMessage: (String) -> Unit,
  parser: IngredientParser = koinInject()
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
    shape = MaterialTheme.shapes.medium,
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
        if (ingredientInput.isNotBlank()) {
          runCatching { parser.parseIngredient(ingredientInput.text) }
            .onSuccess {
              onEdit(it.copy(id = ingredient.id))
              ingredientInput = ingredientInput.copy(text = "")
            }.onFailure {
              onUserMessage("Failed to parse ingredient: ${it.message}")
            }
        } else {
          ingredientInput = ingredientInput.copy(text = "")
          onRemove(ingredient)
        }
      }
    )
  )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InstructionsContent(
  instructions: List<Instruction>,
  ingredients: List<Ingredient>,
  onInstructionChanged: (Instruction) -> Unit,
  onRemoveInstruction: (Instruction) -> Unit,
  onMoveInstruction: (Int, Int) -> Unit,
  onUserMessage: (String) -> Unit,
) {
  val keyboard = LocalSoftwareKeyboardController.current

  val scope = rememberCoroutineScope()
  val lazyListState = rememberLazyListState()
  val reorderableLazyListState = rememberReorderableLazyListState(
    lazyListState = lazyListState,
    scrollThresholdPadding = WindowInsets.systemBars.asPaddingValues()
  ) { from, to ->
    onMoveInstruction(from.index, to.index)
  }

  var reordering by remember { mutableStateOf(false) }

  Column {
    TextButton(
      onClick = { reordering = !reordering },
      enabled = instructions.size > 1,
    ) {
      Text(
        text = if (!reordering) "Reorder instructions" else "Stop reordering",
        style = MaterialTheme.typography.titleMedium
      )
    }

    LazyColumn(
      state = lazyListState,
      verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight()
        .padding(8.dp)
    ) {
      itemsIndexed(
        items = instructions,
        key = { _, instruction -> instruction.id },
        contentType = { _, _ -> "Instruction" }
      ) { index, instruction ->
        ReorderableItem(
          state = reorderableLazyListState,
          key = instruction.id
        ) { isDragging ->
          val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp, label = "Drag and Drop elevation")
          var expanded by remember { mutableStateOf(true) }

          Surface(shadowElevation = elevation) {
            Column {
              InstructionComponent(
                step = index + 1,
                expanded = expanded,
                reordering = reordering,
                reorderScope = this@ReorderableItem,
                instruction = instruction,
                ingredients = ingredients,
                onToggleExpanded = { expanded = it },
                onInstructionChanged = onInstructionChanged,
                onRemoveInstruction = onRemoveInstruction,
              )

              if (instruction != instructions.last()) {
                HorizontalDivider()
              }
            }
          }
        }

      }

      if (!reordering) {
        item {
          var instructionInput by remember { mutableStateOf("") }

          OutlinedTextField(
            value = instructionInput,
            onValueChange = { instructionInput = it },
            modifier = Modifier
              .fillMaxWidth()
              .animateItem(),
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
            minLines = 3,
            keyboardOptions = KeyboardOptions(
              imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
              onDone = {
                if (instructionInput.isNotBlank()) {
                  if (instructionInput.contains("\n")) {
                    instructionInput.split("\n").map { Instruction(it) }.forEach { onInstructionChanged(it) }
                  } else {
                    val instruction = Instruction(instructionInput)
                    onInstructionChanged(instruction)
                  }
                  instructionInput = ""
                  scope.launch { lazyListState.animateScrollToItem(instructions.size) }
                }
                keyboard?.hide()
              }
            )
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun InstructionComponent(
  step: Int,
  expanded: Boolean,
  reordering: Boolean,
  reorderScope: ReorderableCollectionItemScope,
  instruction: Instruction,
  ingredients: List<Ingredient>,
  onToggleExpanded: (Boolean) -> Unit,
  onInstructionChanged: (Instruction) -> Unit,
  onRemoveInstruction: (Instruction) -> Unit,
  modifier: Modifier = Modifier,
) {
  var editing by remember { mutableStateOf(false) }
  var showBottomSheet by remember { mutableStateOf(false) }
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val scope = rememberCoroutineScope()

  val isExpanded = expanded && !reordering

  Column(
    verticalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
      .then(modifier)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier
        .fillMaxWidth()
        .clickable(enabled = !reordering) { onToggleExpanded(!expanded) }
    ) {
      Text(
        text = "Step $step",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
          .wrapContentWidth(Alignment.Start)
      )

      AnimatedContent(targetState = reordering, label = "Swap between drag handle delete button") { isReordering ->
        if (!isReordering) {
          IconButton(
            onClick = {
              onRemoveInstruction(instruction)
            },
            modifier = Modifier
              .wrapContentWidth(Alignment.End)
          ) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "remove instruction")
          }
        } else {
          val view = LocalView.current

          IconButton(
            onClick = {},
            modifier = with(reorderScope) {
              Modifier
                .wrapContentWidth(Alignment.End)
                .draggableHandle(
                  onDragStarted = { view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START) },
                  onDragStopped = { view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END) }
                )
            }

          ) {
            Icon(imageVector = Icons.Default.DragHandle, contentDescription = "reorder instruction")
          }
        }
      }
    }

    AnimatedContent(
      targetState = editing,
      label = "Instruction editing transition",
      transitionSpec = { fadeIn() togetherWith fadeOut() }
    ) { isEditing ->
      if (!isEditing) {
        val maxLines = if (isExpanded) Int.MAX_VALUE else 1

        Text(
          text = instruction.text,
          overflow = TextOverflow.Ellipsis,
          maxLines = maxLines,
          modifier = Modifier
            .fillMaxWidth()
            .clickable { editing = true }
        )
      } else {
        val focusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current
        var focusGained by remember { mutableStateOf(false) }

        InstructionEdit(
          instruction = instruction,
          modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged {
              if (focusGained) {
                if (!it.isFocused || !it.hasFocus) {
                  editing = false
                }
              } else {
                focusGained = it.isFocused || it.hasFocus
              }
            }
            .focusRequester(focusRequester),
          onEdit = {
            onInstructionChanged(it)
            editing = false
          },
          onRemove = {
            onRemoveInstruction(it)
            editing = false
          }
        )

        LaunchedEffect(editing) {
          if (editing) {
            focusRequester.requestFocus()
          } else {
            focusManager.clearFocus()
          }
        }
      }
    }

    AnimatedVisibility(
      visible = isExpanded,
      enter = fadeIn() + expandVertically(),
      exit = fadeOut() + shrinkVertically()
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {

        if (instruction.ingredients.isNotEmpty()) {
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
              .fillMaxWidth()
          ) {
            ingredients.intersect(instruction.ingredients.toSet()).forEach { ingredient ->
              key(ingredient.id) {
                val visible = remember { MutableTransitionState(true) }

                if (!visible.targetState && !visible.currentState && visible.isIdle) {
                  onInstructionChanged(
                    instruction.copy(
                      ingredients = instruction.ingredients - ingredient
                    )
                  )
                }

                AnimatedVisibility(
                  visibleState = visible,
                  exit = fadeOut()
                ) {
                  ItemPill(
                    modifier = Modifier,
                    leadingContent = {
                      if (ingredient.measurement.quantity > 0) {
                        IngredientQuantity(ingredient = ingredient)
                      }
                    },
                    trailingIcon = {
                      IconButton(onClick = { visible.targetState = false }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Remove")
                      }
                    }
                  ) {
                    Text(
                      text = ingredient.name,
                      modifier = Modifier
                        .applyIf(ingredient.measurement.quantity <= 0) {
                          padding(start = 8.dp)
                        }
                    )
                  }
                }
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
            AnimatedContent(
              targetState = instruction.ingredients.isEmpty(),
              label = "Add/Edit Ingredient Icon"
            ) {
              if (it) {
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = "Add ingredients to instruction",
                  modifier = Modifier.padding(8.dp)
                )
              } else {
                Icon(
                  imageVector = Icons.Default.Edit,
                  contentDescription = "Edit instruction's ingredients",
                  modifier = Modifier.padding(8.dp)
                )
              }
            }
          }
        ) {
          AnimatedContent(
            targetState = instruction.ingredients.isEmpty(),
            label = "Add/Edit Ingredient Text"
          ) {
            if (it) {
              Text(
                text = "Add Ingredients",
                modifier = Modifier.padding(end = 8.dp)
              )
            } else {
              Text(
                text = "Edit Ingredients",
                modifier = Modifier.padding(end = 8.dp)
              )
            }
          }
        }
      }
    }
  }

  if (showBottomSheet) {
    val newIngredients = remember { instruction.ingredients.toMutableStateList() }

    ActionBottomSheet(
      onDismissRequest = { showBottomSheet = false },
      sheetState = sheetState,
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
      title = { Text(text = "Select Ingredients") },
      action = {
        TextButton(
          onClick = {
            onInstructionChanged(instruction.copy(ingredients = newIngredients))
            sheetState.dismiss(scope) { showBottomSheet = false }
          },
        ) {
          Text(text = "Save")
        }
      }
    ) {
      LazyColumn(
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.wrapContentSize()
      ) {
        items(ingredients) { ingredient ->
          ItemPill(
            modifier = Modifier.fillMaxWidth(),
            leadingContent = {
              if (ingredient.measurement.quantity > 0) {
                IngredientQuantity(ingredient = ingredient)
              }
            },
            trailingIcon = {
              Checkbox(
                checked = newIngredients.contains(ingredient),
                onCheckedChange = null,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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
              modifier = Modifier.applyIf(ingredient.measurement.quantity <= 0) { padding(start = 8.dp) }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun IngredientQuantity(ingredient: Ingredient) {
  val measurement = ingredient.measurement.normalize { it !is MeasurementUnit.FluidOunce}

  val quantity = measurement.displayQuantity.let {
    if (ingredient.measurement.unit !is MeasurementUnit.None) {
      "$it ${ingredient.measurement.unit.abbreviation}"
    } else {
      it
    }
  }

  // TODO: this works for now, but it should use a custom layout to avoid recomposition
  var minWidth by remember { mutableStateOf(Dp.Unspecified) }
  val density = LocalDensity.current

  Box(
    modifier = Modifier
      .onPlaced {
        minWidth = with(density) {
          it.size.height.toDp()
        }
      }
      .widthIn(min = minWidth)
      .fillMaxHeight()
  ) {
    Text(
      text = quantity,
      color = MaterialTheme.colorScheme.onPrimary,
      fontSize = 18.sp,
      modifier = Modifier
        .padding(8.dp)
        .align(Alignment.Center)
    )
  }
}

@Composable
fun InstructionEdit(
  instruction: Instruction,
  modifier: Modifier = Modifier,
  onEdit: (Instruction) -> Unit = {},
  onRemove: (Instruction) -> Unit = {},
) {
  var instructionInput by remember {
    mutableStateOf(
      TextFieldValue(
        text = instruction.text,
        selection = TextRange(instruction.text.length)
      )
    )
  }

  OutlinedTextField(
    value = instructionInput,
    onValueChange = { instructionInput = it },
    modifier = modifier,
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    keyboardActions = KeyboardActions(
      onDone = {
        if (instructionInput.isNotBlank()) {
          onEdit(instruction.copy(text = instructionInput.text))
          instructionInput = instructionInput.copy(text = "")
        } else {
          instructionInput = instructionInput.copy(text = "")
          if (instruction.ingredients.isEmpty()) {
            onRemove(instruction)
          }
        }
      },
    )
  )
}

private fun TextFieldValue.isNotBlank() = text.isNotBlank()

@Preview
@Composable
fun AddEditRecipeContentPreview() {
  KoinPreview {
    val recipe = koinInject<Recipe>()

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
          onEquipmentChanged = {},
          onMoveIngredient = { _, _ -> },
          onMoveInstruction = { _, _ -> },
          onMoveEquipment = { _, _ -> },
        )
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun IngredientsTabPreview() {
  KoinPreview {
    val recipe = koinInject<Recipe>()

    val ingredients = recipe.ingredients.toMutableStateList()

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
          onMoveIngredient = { from, to ->
            with(ingredients) {
              add(to, removeAt(from))
            }
          },
          onUserMessage = {}
        )
      }
    }
  }
}

@Preview
@Composable
fun IngredientEditPreview() {
  val ingredients = mutableListOf(Ingredient("test", Measurement(1f, MeasurementUnit.Cup), "1 cup test"))
  ingredients.add(Ingredient("test", Measurement(1f, MeasurementUnit.Cup), "1 cup test"))

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
            },
            onUserMessage = {}
          )
        }
      }
    }
  }
}

@Preview
@Composable
fun InstructionsTabPreview() {
  KoinPreview {
    val recipe = koinInject<Recipe>()

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
          onMoveInstruction = { from, to ->
            instructions = instructions.move(from, to)
          },
          onUserMessage = {}
        )
      }
    }
  }
}