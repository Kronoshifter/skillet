package com.kronos.skilletapp.ui.screen.recipe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kronos.skilletapp.model.*
import com.kronos.skilletapp.ui.FabPadding
import com.kronos.skilletapp.ui.LoadingContent
import com.kronos.skilletapp.ui.KoinPreview
import com.kronos.skilletapp.ui.component.IngredientListItem
import com.kronos.skilletapp.ui.component.IngredientRow
import com.kronos.skilletapp.ui.component.IngredientPill
import com.kronos.skilletapp.ui.icon.SkilletIcons
import com.kronos.skilletapp.ui.icon.filled.Skillet
import com.kronos.skilletapp.ui.theme.SkilletAppTheme
import com.kronos.skilletapp.ui.viewmodel.RecipeViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import kotlin.collections.set
import kotlin.math.roundToInt

private enum class RecipeContentTab {
  Ingredients,
  Instructions
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(
  onBack: () -> Unit,
  onEdit: () -> Unit,
  onCook: (scale: Float) -> Unit,
  vm: RecipeViewModel = koinViewModel(),
) {
  val recipeState by vm.recipeState.collectAsStateWithLifecycle()
  val uiState by vm.uiState.collectAsStateWithLifecycle()

  val pagerState = rememberPagerState { RecipeContentTab.entries.size }
  val fabTransitionState = remember { MutableTransitionState(false).apply { targetState = true } }
  val fabTransition = rememberTransition(fabTransitionState, "Fab transition")

  LaunchedEffect(pagerState.isScrollInProgress, fabTransitionState.targetState == fabTransitionState.currentState) {
    fabTransitionState.targetState = !pagerState.isScrollInProgress
  }

  val ingredientListState = rememberLazyListState()
  val instructionsListState = rememberLazyListState()

  val isFabExpanded by remember {
    derivedStateOf {
      (pagerState.currentPage == RecipeContentTab.Ingredients.ordinal && (!ingredientListState.canScrollBackward || !ingredientListState.canScrollForward)) ||
      (pagerState.currentPage == RecipeContentTab.Instructions.ordinal && (!instructionsListState.canScrollBackward || !instructionsListState.canScrollForward))
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { /*Intentionally left empty*/ },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(onClick = onEdit) {
            Icon(Icons.Filled.Edit, contentDescription = "Edit")
          }

          IconButton(onClick = { /*TODO*/ }) {
            Icon(Icons.Filled.MoreVert, contentDescription = "More Options")
          }
        }
      )
    },
    floatingActionButton = {
      fabTransition.AnimatedVisibility(
        visible = { isVisible -> isVisible },
        enter = scaleIn(),
        exit = scaleOut(),
        modifier = Modifier
          .clip(if (isFabExpanded) FloatingActionButtonDefaults.extendedFabShape else FloatingActionButtonDefaults.shape)
      ) {
        ExtendedFloatingActionButton(
          text = { Text("Cook") },
          icon = { Icon(imageVector = SkilletIcons.Filled.Skillet, contentDescription = "Cook") },
          onClick = { onCook(uiState.scale) },
          expanded = isFabExpanded
        )
      }
    }
  ) { paddingValues ->
    LoadingContent(
      state = recipeState,
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
    ) { recipe ->
      RecipeContent(
        recipe = recipe,
        scale = uiState.scale,
        servings = uiState.servings,
        selectedUnits = uiState.selectedUnits,
        onScalingChanged = vm::setScaling,
        onUnitSelect = vm::selectUnit,
        pagerState = pagerState,
        ingredientListState = ingredientListState,
        instructionsListState = instructionsListState,
        modifier = Modifier
          .fillMaxSize()
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun RecipeContent(
  recipe: Recipe,
  scale: Float,
  servings: Int,
  selectedUnits: Map<Ingredient, MeasurementUnit?> = emptyMap(),
  onScalingChanged: (scale: Float, servings: Int) -> Unit,
  onUnitSelect: (Ingredient, MeasurementUnit?) -> Unit,
  pagerState: PagerState = rememberPagerState { RecipeContentTab.entries.size },
  ingredientListState: LazyListState = rememberLazyListState(),
  instructionsListState: LazyListState = rememberLazyListState(),
  modifier: Modifier = Modifier,
) {
  var tab by remember { mutableStateOf(RecipeContentTab.Ingredients) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .then(modifier),
  ) {

    Column(modifier = Modifier.fillMaxSize()) {
      Text(
        text = recipe.name,
        style = MaterialTheme.typography.headlineLarge,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
      )

      //TODO: add recipe image
      //TODO: add recipe time
      //TODO: add source
      //TODO: add notes

      ScalingControls(
        scale = scale,
        servings = servings,
        baseServings = recipe.servings,
        onScalingChanged = onScalingChanged,
      )

      HorizontalDivider(modifier = Modifier.fillMaxWidth())

      PrimaryTabRow(
        selectedTabIndex = tab.ordinal,
        modifier = Modifier.fillMaxWidth()
      ) {
        Tab(
          selected = tab == RecipeContentTab.Ingredients,
          onClick = { tab = RecipeContentTab.Ingredients },
          text = { Text(text = "Ingredients") },
          modifier = Modifier
        )

        Tab(
          selected = tab == RecipeContentTab.Instructions,
          onClick = { tab = RecipeContentTab.Instructions },
          text = { Text(text = "Instructions") },
          modifier = Modifier
        )
      }

      LaunchedEffect(tab) {
        pagerState.animateScrollToPage(tab.ordinal)
      }

      LaunchedEffect(pagerState.targetPage) {
        tab = RecipeContentTab.entries[pagerState.targetPage]
      }

      HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxWidth()
      ) {
        val page = RecipeContentTab.entries[it]
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          when (page) {
            RecipeContentTab.Ingredients -> IngredientsList(
              ingredients = recipe.ingredients,
              scale = scale,
              selectedUnits = selectedUnits,
              onUnitSelect = onUnitSelect,
              listState = ingredientListState,
              listPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp + FabPadding)
            )

            RecipeContentTab.Instructions -> InstructionsList(
              instructions = recipe.instructions,
              scale = scale,
              selectedUnits = selectedUnits,
              onUnitSelect = onUnitSelect,
              listState = instructionsListState,
              listPadding = PaddingValues(top = 8.dp, bottom = 8.dp + FabPadding)
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScalingControls(
  scale: Float,
  servings: Int,
  baseServings: Int,
  onScalingChanged: (scale: Float, servings: Int) -> Unit,
  maxScale: Int = 3,
) {
  val scaleOptions = 1..maxScale

  Row(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .padding(8.dp)
      .fillMaxWidth()
      .height(IntrinsicSize.Min)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      OutlinedIconButton(
        onClick = {
          val newServings = (servings - 1).coerceAtLeast(1)
          val newScale = newServings / baseServings.toFloat()
          onScalingChanged(newScale, newServings)
        },
        enabled = servings > 1,
      ) {
        Icon(imageVector = Icons.Filled.Remove, contentDescription = null)
      }

      val textMeasurer = rememberTextMeasurer()
      val result = textMeasurer.measure(
        AnnotatedString("00 servings"),
        style = LocalTextStyle.current
      )
      val textWidth = with(LocalDensity.current) {
        result.size.width.toDp()
      }

      Text(
        text = "$servings servings",
        maxLines = 1,
        textAlign = TextAlign.Center,
        modifier = Modifier
          .width(textWidth)
      )

      OutlinedIconButton(
        onClick = {
          val newServings = servings + 1
          val newScale = newServings / baseServings.toFloat()
          onScalingChanged(newScale, newServings)
        },
//        modifier = Modifier.weight(1f)
      ) {
        Icon(imageVector = Icons.Filled.Add, contentDescription = null)
      }
    }

    SingleChoiceSegmentedButtonRow(
      modifier = Modifier
        .width(IntrinsicSize.Min)
        .weight(1f)
    ) {
      scaleOptions.forEach { option ->
        val selected = scale == option.toFloat()
        SegmentedButton(
          selected = selected,
          onClick = {
            val newScale = option.toFloat()
            val newServings = (baseServings * newScale).roundToInt()
            onScalingChanged(newScale, newServings)
          },
          shape = when (option) {
            scaleOptions.first() -> RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50)
            scaleOptions.last() -> RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50)
            else -> RectangleShape
          },
          icon = {} // this is silly, why isn't this nullable?
        ) {
          Text(
            text = "${option}x",
            color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientsList(
  ingredients: List<Ingredient>,
  scale: Float,
  selectedUnits: Map<Ingredient, MeasurementUnit?>,
  onUnitSelect: (Ingredient, MeasurementUnit?) -> Unit,
  listState: LazyListState = rememberLazyListState(),
  listPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
) {
  if (ingredients.isEmpty()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text(text = "No Ingredients", color = MaterialTheme.colorScheme.secondary)
    }
    return
  }

  //TODO: sort ingredients so that quantity-based ingredients come first

  LazyColumn(
    state = listState,
    modifier = Modifier.fillMaxSize(),
    contentPadding = listPadding,
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    items(
      items = ingredients,
      key = { it.id }
    ) { ingredient ->
      IngredientListItem(
        ingredient = ingredient,
        scale = scale,
        selectedUnit = selectedUnits[ingredient],
        onUnitSelect = onUnitSelect,
      )
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InstructionsList(
  instructions: List<Instruction>,
  scale: Float,
  selectedUnits: Map<Ingredient, MeasurementUnit?>,
  onUnitSelect: (Ingredient, MeasurementUnit?) -> Unit,
  listState: LazyListState = rememberLazyListState(),
  listPadding: PaddingValues = PaddingValues(vertical = 8.dp)
) {
  if (instructions.isEmpty()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text(text = "No Instructions", color = MaterialTheme.colorScheme.secondary)
    }
    return
  }

  LazyColumn(
    state = listState,
    modifier = Modifier.fillMaxSize(),
    contentPadding = listPadding,
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    itemsIndexed(instructions) { index, instruction ->
      InstructionComponent(
        step = index + 1,
        instruction = instruction,
        scale = scale,
        selectedUnits = selectedUnits,
        onUnitSelect = onUnitSelect
      )

      if (instruction != instructions.last()) {
        HorizontalDivider()
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun InstructionComponent(
  step: Int,
  instruction: Instruction,
  scale: Float,
  selectedUnits: Map<Ingredient, MeasurementUnit?>,
  onUnitSelect: (Ingredient, MeasurementUnit?) -> Unit,
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
  ) {
    Text(
      text = "Step $step",
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold,
      modifier = Modifier
        .fillMaxWidth()
    )

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
          key(ingredient.id) {
            IngredientPill(
              ingredient = ingredient,
              scale = scale,
              selectedUnit = selectedUnits[ingredient],
              onUnitSelect = onUnitSelect
            )
          }
        }
      }
    }
  }
}

/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
//////////////////// PREVIEWS ///////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////

@Preview
@Composable
private fun RecipeContentPreview() {
  KoinPreview {
    val recipe = koinInject<Recipe>()

    val selectedUnits = remember { mutableStateMapOf<Ingredient, MeasurementUnit?>() }

    var scale by remember { mutableFloatStateOf(1f) }
    var servings by remember { mutableIntStateOf(recipe.servings) }

    SkilletAppTheme {
      Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        RecipeContent(
          recipe = recipe,
          selectedUnits = selectedUnits,
          onUnitSelect = { ingredient, unit -> selectedUnits[ingredient] = unit },
          onScalingChanged = { newScale, newServings ->
            scale = newScale
            servings = newServings
          },
          scale = scale,
          servings = servings
        )
      }
    }
  }
}

@Preview(showBackground = true, device = "spec:parent=pixel_5")
@Composable
private fun IngredientsListEmptyPreview() {
  val ingredients = emptyList<Ingredient>()
  val selectedUnits = remember { mutableStateMapOf<Ingredient, MeasurementUnit?>() }

  SkilletAppTheme {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      IngredientsList(
        ingredients = ingredients,
        scale = 1f,
        selectedUnits = selectedUnits,
        onUnitSelect = { ingredient, unit -> selectedUnits[ingredient] = unit }
      )
    }
  }
}

@Preview
@Composable
private fun IngredientListPreview() {
  KoinPreview {
    val recipe = koinInject<Recipe>()
    val ingredients = recipe.ingredients
    val selectedUnits = remember { mutableStateMapOf<Ingredient, MeasurementUnit?>() }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      IngredientsList(
        ingredients = ingredients,
        scale = 1f,
        selectedUnits = selectedUnits,
        onUnitSelect = { ingredient, unit -> selectedUnits[ingredient] = unit }
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun IngredientComponentPreview() {
  val ingredient =
    Ingredient("Mini Shells Pasta", measurement = Measurement(8f, MeasurementUnit.Ounce), raw = "8 oz Mini Shells Pasta")

  Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    IngredientRow(
      ingredient = ingredient,
      scale = 1f,
      selectedUnit = null,
      enabled = false,
      onClick = {}
    )
  }
}

@Preview
@Composable
private fun InstructionsListEmptyPreview() {
  SkilletAppTheme {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      InstructionsList(instructions = emptyList(), scale = 1f, selectedUnits = emptyMap(), onUnitSelect = { _, _ -> })
    }
  }
}

@Preview
@Composable
private fun InstructionsListPreview() {
  KoinPreview {
    val recipe = koinInject<Recipe>()
    val instructions = recipe.instructions
    val selectedUnits = remember { mutableStateMapOf<Ingredient, MeasurementUnit?>() }

    SkilletAppTheme {
      Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        InstructionsList(
          instructions = instructions,
          scale = 1f,
          selectedUnits = selectedUnits,
          onUnitSelect = { ingredient, unit -> selectedUnits[ingredient] = unit }
        )
      }
    }
  }
}

@Preview
@Composable
private fun InstructionComponentPreview() {
  KoinPreview {
    val recipe = koinInject<Recipe>()
    val instructions = recipe.instructions

    val selectedUnits = remember { mutableStateMapOf<Ingredient, MeasurementUnit?>() }

    SkilletAppTheme {
      Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.padding(16.dp)) {
          InstructionComponent(
            step = 1,
            instruction = instructions.first(),
            scale = 1f,
            selectedUnits = selectedUnits,
            onUnitSelect = { ingredient, unit -> selectedUnits[ingredient] = unit }
          )
        }
      }
    }
  }
}