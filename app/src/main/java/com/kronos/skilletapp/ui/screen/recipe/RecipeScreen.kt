package com.kronos.skilletapp.ui.screen.recipe

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.michaelbull.result.unwrap
import com.kronos.skilletapp.data.RecipeRepository
import com.kronos.skilletapp.model.*
import com.kronos.skilletapp.ui.LoadingContent
import com.kronos.skilletapp.ui.component.IngredientRow
import com.kronos.skilletapp.ui.component.IngredientPill
import com.kronos.skilletapp.ui.component.UnitSelectionBottomSheet
import com.kronos.skilletapp.ui.theme.SkilletAppTheme
import com.kronos.skilletapp.ui.viewmodel.RecipeViewModel
import com.kronos.skilletapp.utils.Fraction
import com.kronos.skilletapp.utils.toFraction
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.androidx.compose.getViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.KoinApplication
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import kotlin.collections.set
import kotlin.math.roundToInt

private object RecipeContentTab {
  const val INGREDIENTS = 0
  const val INSTRUCTIONS = 1
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(
  onBack: () -> Unit,
  onEdit: () -> Unit = {},
  vm: RecipeViewModel = getViewModel(),
) {
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
          IconButton(onClick = { /*TODO*/ }) {
            Icon(Icons.Filled.Edit, contentDescription = "Edit")
          }

          IconButton(onClick = { /*TODO*/ }) {
            Icon(Icons.Filled.MoreVert, contentDescription = "More Options")
          }
        }
      )
    }
  ) { paddingValues ->
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val selectedUnits by vm.selectedUnits.collectAsStateWithLifecycle()

    LoadingContent(
      state = uiState,
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
    ) { data ->
      RecipeContent(
        recipe = data.recipe,
        selectedUnits = selectedUnits,
        onUnitSelect = vm::selectUnit,
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
  selectedUnits: Map<Ingredient, MeasurementUnit?> = emptyMap(),
  onUnitSelect: (Ingredient, MeasurementUnit?) -> Unit,
  modifier: Modifier = Modifier,
) {
  var tab by remember { mutableIntStateOf(RecipeContentTab.INGREDIENTS) }
  val pagerState = rememberPagerState { 2 }

  Surface(
    modifier = Modifier
      .fillMaxSize()
      .then(modifier),
    color = MaterialTheme.colorScheme.background
  ) {

    Column(modifier = Modifier.fillMaxSize()) {
      Text(
        text = recipe.name,
        style = MaterialTheme.typography.headlineLarge,
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      )

      var scale by remember { mutableDoubleStateOf(1.0) }
      var servings by remember { mutableIntStateOf(recipe.servings) }

      ScalingControls(
        scale = scale,
        servings = servings,
        baseServings = recipe.servings,
        onScalingChanged = { newScale, newServings ->
          scale = newScale
          servings = newServings
        },
      )

      HorizontalDivider(modifier = Modifier.fillMaxWidth())

      PrimaryTabRow(
        selectedTabIndex = tab,
        modifier = Modifier.fillMaxWidth()
      ) {
        Tab(
          selected = tab == RecipeContentTab.INGREDIENTS,
          onClick = { tab = RecipeContentTab.INGREDIENTS },
          text = { Text(text = "Ingredients") },
          modifier = Modifier
        )

        Tab(
          selected = tab == RecipeContentTab.INSTRUCTIONS,
          onClick = { tab = RecipeContentTab.INSTRUCTIONS },
          text = { Text(text = "Instructions") },
          modifier = Modifier
        )
      }

      LaunchedEffect(tab) {
        pagerState.animateScrollToPage(tab)
      }

      LaunchedEffect(pagerState.targetPage) {
        tab = pagerState.targetPage
      }

      HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxWidth()
      ) { page ->
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          when (page) {
            RecipeContentTab.INGREDIENTS -> IngredientsList(
              ingredients = recipe.ingredients,
              scale = scale,
              selectedUnits = selectedUnits,
              onUnitSelect = onUnitSelect
            )

            RecipeContentTab.INSTRUCTIONS -> InstructionsList(
              instructions = recipe.instructions,
              scale = scale,
              selectedUnits = selectedUnits,
              onUnitSelect = onUnitSelect
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
  scale: Double,
  servings: Int,
  baseServings: Int,
  onScalingChanged: (scale: Double, servings: Int) -> Unit,
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
          val newScale = newServings / baseServings.toDouble()
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
          val newScale = newServings / baseServings.toDouble()
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
        val selected = scale == option.toDouble()
        SegmentedButton(
          selected = selected,
          onClick = {
            val newScale = option.toDouble()
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
  scale: Double,
  selectedUnits: Map<Ingredient, MeasurementUnit?>,
  onUnitSelect: (Ingredient, MeasurementUnit?) -> Unit,
) {
  if (ingredients.isEmpty()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text(text = "No Ingredients", color = MaterialTheme.colorScheme.secondary)
    }
    return
  }

  //TODO: sort ingredients so that quantity-based ingredients come first

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    items(ingredients) { ingredient ->
      val selectedUnit = selectedUnits[ingredient]
      val measurements = MeasurementUnit.values
        .filter { it.type == ingredient.measurement.unit.type }
        .map { ingredient.measurement.convert(it).scale(scale) }
        .filter { it.quantity.toFraction().roundToNearestFraction().reduce() > Fraction(1, 8) }

      var showBottomSheet by remember { mutableStateOf(false) }

      IngredientRow(
        ingredient = ingredient,
        scale = scale,
        selectedUnit = selectedUnit,
        enabled = measurements.isNotEmpty(),
        onClick = { showBottomSheet = true },
      )

      val sheetState = rememberModalBottomSheetState()
      val scope = rememberCoroutineScope()

      if (showBottomSheet) {
        UnitSelectionBottomSheet(
          onDismissRequest = { showBottomSheet = false },
          onUnitSelect = {
            onUnitSelect(ingredient, it.takeIf { selectedUnit != it })

            scope.launch { sheetState.hide() }.invokeOnCompletion {
              if (!sheetState.isVisible) {
                showBottomSheet = false
              }
            }
          },
          ingredient = ingredient,
          measurements = measurements,
          selectedUnit = selectedUnit,
          sheetState = sheetState
        )
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InstructionsList(
  instructions: List<Instruction>,
  scale: Double,
  selectedUnits: Map<Ingredient, MeasurementUnit?>,
  onUnitSelect: (Ingredient, MeasurementUnit?) -> Unit,
) {
  if (instructions.isEmpty()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text(text = "No Instructions", color = MaterialTheme.colorScheme.secondary)
    }
    return
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    itemsIndexed(instructions) { index, instruction ->
      Text(
        text = "Step ${index + 1}",
        fontWeight = FontWeight.Bold,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp)
      )

      InstructionComponent(
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
  instruction: Instruction,
  scale: Double,
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
          IngredientPill(ingredient, scale, selectedUnits, onUnitSelect)
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
  val repository = RecipeRepository()
  val recipe = runBlocking { repository.fetchRecipe("test") }

  val selectedUnits = remember { mutableStateMapOf<Ingredient, MeasurementUnit?>() }

  SkilletAppTheme {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      RecipeContent(
        recipe = recipe.unwrap(),
        selectedUnits = selectedUnits,
        onUnitSelect = { ingredient, unit -> selectedUnits[ingredient] = unit }
      )
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
        scale = 1.0,
        selectedUnits = selectedUnits,
        onUnitSelect = { ingredient, unit -> selectedUnits[ingredient] = unit }
      )
    }
  }
}

@Preview
@Composable
private fun IngredientListPreview() {
  val repository = RecipeRepository()

//  val ingredients = listOf(
//    Ingredient("Mini Shells Pasta", IngredientType.Dry, measurement = Measurement(8.0, MeasurementUnit.Ounce)),
//    Ingredient("Olive Oil", IngredientType.Wet, measurement = Measurement(1.0, MeasurementUnit.Tablespoon)),
//    Ingredient("Butter", IngredientType.Wet, measurement = Measurement(1.0, MeasurementUnit.Tablespoon)),
//    Ingredient(
//      "Garlic",
//      IngredientType.Dry,
//      measurement = Measurement(2.0, MeasurementUnit.Custom("clove", "clove"))
//    ),
//    Ingredient("Flour", IngredientType.Dry, measurement = Measurement(2.0, MeasurementUnit.Tablespoon)),
//    Ingredient("Chicken Broth", IngredientType.Wet, measurement = Measurement(0.75, MeasurementUnit.Cup)),
//    Ingredient("Milk", IngredientType.Wet, measurement = Measurement(2.5, MeasurementUnit.Cup)),
//  )

  val ingredients = runBlocking { repository.fetchRecipe("test").unwrap().ingredients }
  val selectedUnits = remember { mutableStateMapOf<Ingredient, MeasurementUnit?>() }

  Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    IngredientsList(
      ingredients = ingredients,
      scale = 1.0,
      selectedUnits = selectedUnits,
      onUnitSelect = { ingredient, unit -> selectedUnits[ingredient] = unit }
    )
  }

}

@Preview(showBackground = true)
@Composable
private fun IngredientComponentPreview() {
  val ingredient =
    Ingredient("Mini Shells Pasta", measurement = Measurement(8.0, MeasurementUnit.Ounce))

  Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    IngredientRow(
      ingredient = ingredient,
      scale = 1.0, null,
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
      InstructionsList(instructions = emptyList(), scale = 1.0, selectedUnits = emptyMap(), onUnitSelect = { _, _ -> })
    }
  }
}

@Preview
@Composable
private fun InstructionsListPreview() {
  val repository = RecipeRepository()
  val instructions = runBlocking { repository.fetchRecipe("test").unwrap().instructions }
  val selectedUnits = remember { mutableStateMapOf<Ingredient, MeasurementUnit?>() }

  SkilletAppTheme {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      InstructionsList(
        instructions = instructions,
        scale = 1.0,
        selectedUnits = selectedUnits,
        onUnitSelect = { ingredient, unit -> selectedUnits[ingredient] = unit }
      )
    }
  }
}

@Preview
@Composable
private fun InstructionComponentPreview() {
  val instruction = Instruction(
    text = "Return pot to stove over medium heat then ass butter and olive oil. Once melted, add garlic then saute until light golden brown, about 30 seconds, being very careful not to burn. Sprinkle in flour then whisk and saute for 1 minute. Slowly pour in chicken broth and milk while whisking until mixture is smooth. Season with salt and pepper then switch to a wooden spoon and stir constantly until mixture is thick and bubbly, 4-5 minutes.",
    ingredients = listOf(
      Ingredient("Butter", measurement = Measurement(1.0, MeasurementUnit.Tablespoon)),
      Ingredient("Olive Oil", measurement = Measurement(1.0, MeasurementUnit.Tablespoon)),
      Ingredient(
        "Garlic",
        measurement = Measurement(2.0, MeasurementUnit.Custom("clove"))
      ),
      Ingredient("Flour", measurement = Measurement(2.0, MeasurementUnit.Tablespoon)),
      Ingredient("Chicken Broth", measurement = Measurement(0.75, MeasurementUnit.Cup)),
      Ingredient("Milk", measurement = Measurement(2.5, MeasurementUnit.Cup)),
    )
  )

  val selectedUnits = remember { mutableStateMapOf<Ingredient, MeasurementUnit?>() }

  SkilletAppTheme {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      Box(modifier = Modifier.padding(16.dp)) {
        InstructionComponent(
          instruction = instruction,
          scale = 1.0,
          selectedUnits = selectedUnits,
          onUnitSelect = { ingredient, unit -> selectedUnits[ingredient] = unit }
        )
      }
    }
  }
}