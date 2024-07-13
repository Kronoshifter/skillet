package com.kronos.skilletapp.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.michaelbull.result.*
import com.kronos.skilletapp.data.RecipeRepository
import com.kronos.skilletapp.model.*
import com.kronos.skilletapp.ui.theme.SkilletAppTheme
import com.kronos.skilletapp.ui.viewmodel.RecipeViewModel
import com.kronos.skilletapp.utils.Fraction
import com.kronos.skilletapp.utils.applyIf
import com.kronos.skilletapp.utils.toFraction
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import org.koin.androidx.compose.getViewModel

private object RecipeContentTab {
  const val INGREDIENTS = 0
  const val INSTRUCTIONS = 1
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(
  id: String,
  onBack: () -> Unit,
  vm: RecipeViewModel = getViewModel(),
) {
  vm.fetchRecipe(id)
  val recipeState by vm.recipe.collectAsStateWithLifecycle()

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
    AnimatedContent(targetState = recipeState, label = "loading") { targetState ->
      targetState.onSuccess { recipe ->
        RecipeContent(
          recipe = recipe,
          modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
        )
      }.onFailure { error ->
        Box(
          modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(), contentAlignment = Alignment.Center
        ) {
          CircularProgressIndicator()
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun RecipeContent(
  recipe: Recipe,
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
            RecipeContentTab.INGREDIENTS -> IngredientsList(recipe.ingredients, scale)
            RecipeContentTab.INSTRUCTIONS -> InstructionsList(recipe.instructions, scale)
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
  vm: RecipeViewModel = getViewModel(),
) {
  if (ingredients.isEmpty()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text(text = "No Ingredients", color = MaterialTheme.colorScheme.secondary)
    }
    return
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    items(ingredients) { ingredient ->
      val selectedUnit = vm.getSelectedUnit(ingredient)
      val measurements = MeasurementUnit.values
        .filter { it.type == ingredient.measurement.unit.type }
        .map { ingredient.measurement.convert(it).scale(scale) }
        .filter { it.quantity.toFraction().roundToNearestFraction().reduce() > Fraction(1, 8) }

      var showBottomSheet by remember { mutableStateOf(false) }

      IngredientComponent(
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
            vm.selectUnit(ingredient, it.takeIf { selectedUnit != it })

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

@Composable
private fun IngredientComponent(
  ingredient: Ingredient,
  scale: Double,
  selectedUnit: MeasurementUnit?,
  enabled: Boolean,
  onClick: () -> Unit,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier
      .fillMaxWidth()
      .height(IntrinsicSize.Min),
  ) {

    Box(
      modifier = Modifier
        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
        .clip(RoundedCornerShape(percent = 25))
        .background(MaterialTheme.colorScheme.primary)
        .applyIf(selectedUnit != null) {
          border(2.dp, MaterialTheme.colorScheme.onSecondaryContainer, RoundedCornerShape(percent = 25))
        }
        .clickable(enabled = enabled, onClick = onClick),
      contentAlignment = Alignment.Center
    ) {
      val measurement = ingredient.measurement.scale(scale)
        .run { selectedUnit?.let { convert(it) } ?: normalize { it !is MeasurementUnit.FluidOunce } }
      val quantity = when (measurement.unit.system) {
        MeasurementSystem.Metric -> measurement.quantity.toString().take(4).removeSuffix(".")
        else -> measurement.quantity.toFraction().roundToNearestFraction().reduce().toString()
      }

      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround,
        modifier = Modifier.align(Alignment.Center)
      ) {
        Text(
          text = quantity,
          color = MaterialTheme.colorScheme.onPrimary,
          fontSize = 18.sp,
          modifier = Modifier.offset(y = 4.dp)
        )

        Text(
          text = measurement.unit.abbreviation,
          color = MaterialTheme.colorScheme.onPrimary,
          fontSize = 12.sp
        )
      }
    }

    Text(
      text = ingredient.name.lowercase(),
      fontWeight = FontWeight.Bold
    )
  }


}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun UnitSelectionBottomSheet(
  onDismissRequest: () -> Unit,
  onUnitSelect: (MeasurementUnit) -> Unit,
  ingredient: Ingredient,
  measurements: List<Measurement>,
  selectedUnit: MeasurementUnit?,
  sheetState: SheetState = rememberModalBottomSheetState(),
) {
  ModalBottomSheet(
    sheetState = sheetState,
    onDismissRequest = onDismissRequest,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
    ) {
      Text(
        text = ingredient.name,
        style = MaterialTheme.typography.titleLarge,
      )

      LazyVerticalGrid(
        columns = GridCells.Adaptive(48.dp),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        items(measurements) { measurement ->
          val quantity = when (measurement.unit.system) {
            MeasurementSystem.Metric -> measurement.quantity.toString().take(4).removeSuffix(".")
            else -> measurement.quantity.toFraction().roundToNearestFraction().reduce().toString()
          }

          Box(
            modifier = Modifier
              .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
              .clip(RoundedCornerShape(percent = 25))
              .background(MaterialTheme.colorScheme.primary)
              .applyIf(selectedUnit == measurement.unit) {
                border(
                  width = 2.dp,
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                  shape = RoundedCornerShape(percent = 25)
                )
              }
              .clickable { onUnitSelect(measurement.unit) }
          ) {

            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.SpaceAround,
              modifier = Modifier
                .align(Alignment.Center)
                .padding(4.dp)
            ) {
              Text(
                text = quantity,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 18.sp,
                modifier = Modifier.offset(y = 4.dp)
              )

              Text(
                text = measurement.unit.abbreviation,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 12.sp
              )
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InstructionsList(
  instructions: List<Instruction>,
  scale: Double,
  vm: RecipeViewModel = getViewModel(),
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
  vm: RecipeViewModel = getViewModel(),
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
          var showBottomSheet by remember { mutableStateOf(false) }

          val measurements = MeasurementUnit.values
            .filter { it.type == ingredient.measurement.unit.type }
            .map { ingredient.measurement.convert(it).scale(scale) }
            .filter { it.quantity.toFraction().roundToNearestFraction().reduce() > Fraction(1, 8) }

          val selectedUnit = vm.getSelectedUnit(ingredient)

          val borderColor =
            selectedUnit?.let { MaterialTheme.colorScheme.onSecondaryContainer } ?: MaterialTheme.colorScheme.primary

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
              .width(IntrinsicSize.Max)
              .height(IntrinsicSize.Max)
              .clip(CircleShape)
              .border(width = 2.dp, color = borderColor, shape = CircleShape)
              .clickable(enabled = measurements.isNotEmpty()) { showBottomSheet = true },
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            ) {
              val measurement = ingredient.measurement.scale(scale).run {
                selectedUnit?.let { convert(it) } ?: normalize { it !is MeasurementUnit.FluidOunce }
              }

              val quantity = when (measurement.unit.system) {
                MeasurementSystem.Metric -> measurement.quantity.toString().take(4).removeSuffix(".")
                else -> measurement.quantity.toFraction().roundToNearestFraction().reduce().toString()
              }

              Text(
                text = "$quantity ${measurement.unit.abbreviation}",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 18.sp,
                modifier = Modifier.padding(8.dp)
              )
            }

            Text(
              text = ingredient.name,
              modifier = Modifier
                .padding(end = 16.dp)
            )
          }

          val sheetState = rememberModalBottomSheetState()
          val scope = rememberCoroutineScope()

          if (showBottomSheet) {
            UnitSelectionBottomSheet(
              onDismissRequest = { showBottomSheet = false },
              onUnitSelect = {
                vm.selectUnit(ingredient, it.takeIf { selectedUnit != it })
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
  }
}

/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
//////////////////// PREVIEWS ///////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////

@Preview
@Composable
private fun RecipePagePreview() {
  val vm = RecipeViewModel(RecipeRepository())
  vm.fetchRecipe("test")

  val recipe by vm.recipe.collectAsState()

  SkilletAppTheme {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      RecipeContent(
        recipe = recipe.unwrap(),
      )
    }
  }
}

@Preview(showBackground = true, device = "spec:parent=pixel_5")
@Composable
private fun IngredientsListEmptyPreview() {
  val ingredients = emptyList<Ingredient>()
  val vm = RecipeViewModel(RecipeRepository())

  SkilletAppTheme {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      IngredientsList(ingredients = ingredients, scale = 1.0, vm = vm)
    }
  }
}

@Preview
@Composable
private fun IngredientListPreview() {
  val vm = RecipeViewModel(RecipeRepository())

  val ingredients = listOf(
    Ingredient("Mini Shells Pasta", IngredientType.Dry, measurement = Measurement(8.0, MeasurementUnit.Ounce)),
    Ingredient("Olive Oil", IngredientType.Wet, measurement = Measurement(1.0, MeasurementUnit.Tablespoon)),
    Ingredient("Butter", IngredientType.Wet, measurement = Measurement(1.0, MeasurementUnit.Tablespoon)),
    Ingredient(
      "Garlic",
      IngredientType.Dry,
      measurement = Measurement(2.0, MeasurementUnit.Custom("clove", "clove"))
    ),
    Ingredient("Flour", IngredientType.Dry, measurement = Measurement(2.0, MeasurementUnit.Tablespoon)),
    Ingredient("Chicken Broth", IngredientType.Wet, measurement = Measurement(0.75, MeasurementUnit.Cup)),
    Ingredient("Milk", IngredientType.Wet, measurement = Measurement(2.5, MeasurementUnit.Cup)),
  )

  Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    IngredientsList(ingredients = ingredients, scale = 1.0, vm = vm)
  }

}

@Preview(showBackground = true)
@Composable
private fun IngredientComponentPreview() {
  val ingredient =
    Ingredient("Mini Shells Pasta", IngredientType.Dry, measurement = Measurement(8.0, MeasurementUnit.Ounce))

  Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    IngredientComponent(
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
  val vm = RecipeViewModel(RecipeRepository())

  val instructions = listOf(
    Instruction("Cook pasta in a pot of salted boiling water until al dente"),
    Instruction("Return pot to stove over medium heat then ass butter and olive oil. Once melted, add garlic then saute until light golden brown, about 30 seconds, being very careful not to burn. Sprinkle in flour then whisk and saute for 1 minute. Slowly pour in chicken broth and milk while whisking until mixture is smooth. Season with salt and pepper then switch to a wooden spoon and stir constantly until mixture is thick and bubbly, 4.-5 minutes."),
    Instruction("Remove pot from heat then stir in parmesan cheese, garlic powder, and parsley flakes until smooth. Add cooked pasta then stir to combine. Taste then adjust salt and pepper if necessary, and then serve."),
  )

  SkilletAppTheme {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      InstructionsList(instructions = emptyList(), scale = 1.0, vm = vm)
    }
  }
}

@Preview
@Composable
private fun InstructionsListPreview() {
  val vm = RecipeViewModel(RecipeRepository())

  val instructions = listOf(
    Instruction(
      text = "Cook pasta in a pot of salted boiling water until al dente",
      ingredients = listOf(
        Ingredient(
          "Pasta",
          IngredientType.Dry,
          measurement = Measurement(8.0, MeasurementUnit.Ounce)
        ),
        Ingredient("Olive Oil", IngredientType.Wet, measurement = Measurement(1.0, MeasurementUnit.Tablespoon)),
      )
    ),
    Instruction(
      text = "Return pot to stove over medium heat then ass butter and olive oil. Once melted, add garlic then saute until light golden brown, about 30 seconds, being very careful not to burn. Sprinkle in flour then whisk and saute for 1 minute. Slowly pour in chicken broth and milk while whisking until mixture is smooth. Season with salt and pepper then switch to a wooden spoon and stir constantly until mixture is thick and bubbly, 4.-5 minutes.",
      ingredients = listOf(
        Ingredient("Butter", IngredientType.Wet, measurement = Measurement(1.0, MeasurementUnit.Tablespoon)),
        Ingredient("Olive Oil", IngredientType.Wet, measurement = Measurement(1.0, MeasurementUnit.Tablespoon)),
        Ingredient(
          "Garlic",
          IngredientType.Dry,
          measurement = Measurement(2.0, MeasurementUnit.Custom("clove", "clove"))
        ),
        Ingredient("Flour", IngredientType.Dry, measurement = Measurement(2.0, MeasurementUnit.Tablespoon)),
        Ingredient("Chicken Broth", IngredientType.Wet, measurement = Measurement(0.75, MeasurementUnit.Cup)),
        Ingredient("Milk", IngredientType.Wet, measurement = Measurement(2.5, MeasurementUnit.Cup)),
      )
    ),
    Instruction(
      text = "Remove pot from heat then stir in parmesan cheese, garlic powder, and parsley flakes until smooth. Add cooked pasta then stir to combine. Taste then adjust salt and pepper if necessary, and then serve.",
      ingredients = emptyList()
    ),
  )

  SkilletAppTheme {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      InstructionsList(instructions = instructions, scale = 1.0, vm = vm)
    }
  }
}

@Preview
@Composable
private fun InstructionComponentPreview() {
  val vm = RecipeViewModel(RecipeRepository())

  val instruction = Instruction(
    text = "Return pot to stove over medium heat then ass butter and olive oil. Once melted, add garlic then saute until light golden brown, about 30 seconds, being very careful not to burn. Sprinkle in flour then whisk and saute for 1 minute. Slowly pour in chicken broth and milk while whisking until mixture is smooth. Season with salt and pepper then switch to a wooden spoon and stir constantly until mixture is thick and bubbly, 4-5 minutes.",
    ingredients = listOf(
      Ingredient("Butter", IngredientType.Wet, measurement = Measurement(1.0, MeasurementUnit.Tablespoon)),
      Ingredient("Olive Oil", IngredientType.Wet, measurement = Measurement(1.0, MeasurementUnit.Tablespoon)),
      Ingredient(
        "Garlic",
        IngredientType.Dry,
        measurement = Measurement(2.0, MeasurementUnit.Custom("clove", "clove"))
      ),
      Ingredient("Flour", IngredientType.Dry, measurement = Measurement(2.0, MeasurementUnit.Tablespoon)),
      Ingredient("Chicken Broth", IngredientType.Wet, measurement = Measurement(0.75, MeasurementUnit.Cup)),
      Ingredient("Milk", IngredientType.Wet, measurement = Measurement(2.5, MeasurementUnit.Cup)),
    )
  )

  SkilletAppTheme {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      Box(modifier = Modifier.padding(16.dp)) {
        InstructionComponent(instruction = instruction, scale = 1.0, vm = vm)
      }
    }
  }
}