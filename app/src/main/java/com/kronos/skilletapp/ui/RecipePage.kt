package com.kronos.skilletapp.ui

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
import androidx.compose.material.icons.filled.Add
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
import com.kronos.skilletapp.model.*
import com.kronos.skilletapp.ui.theme.SkilletAppTheme
import com.kronos.skilletapp.ui.viewmodel.RecipePageViewModel
import com.kronos.skilletapp.utils.Fraction
import com.kronos.skilletapp.utils.toFraction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import org.koin.androidx.compose.getViewModel

object RecipePage {
  const val INGREDIENTS = 0
  const val INSTRUCTIONS = 1
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RecipePage(
  recipe: Recipe,
) {
  var tab by remember { mutableIntStateOf(RecipePage.INGREDIENTS) }
  val pagerState = rememberPagerState { 2 }

  SkilletAppTheme {
    Surface(
      modifier = Modifier.fillMaxSize(),
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
            selected = tab == RecipePage.INGREDIENTS,
            onClick = { tab = RecipePage.INGREDIENTS },
            text = { Text(text = "Ingredients") },
            modifier = Modifier
          )

          Tab(
            selected = tab == RecipePage.INSTRUCTIONS,
            onClick = { tab = RecipePage.INSTRUCTIONS },
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
              RecipePage.INGREDIENTS -> IngredientsList(recipe.ingredients, scale)
              RecipePage.INSTRUCTIONS -> InstructionsList(recipe.instructions, scale)
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScalingControls(
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
      modifier = Modifier
//        .weight(1f)
    ) {
      OutlinedIconButton(
        onClick = {
          val newServings = (servings - 1).coerceAtLeast(1)
          val newScale = newServings / baseServings.toDouble()
          onScalingChanged(newScale, newServings)
        },
        enabled = servings > 1,
//        modifier = Modifier.weight(1f)
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
//          .padding(horizontal = 8.dp)
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

@Composable
fun IngredientsList(
  ingredients: List<Ingredient>,
  scale: Double,
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
      IngredientComponent(
        ingredient = ingredient,
        scale = scale
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientComponent(
  ingredient: Ingredient,
  scale: Double,
) {
  val vm = getViewModel<RecipePageViewModel>()

  var showBottomSheet by remember { mutableStateOf(false) }

  val measurements = MeasurementUnit.values
    .filter { it.type == ingredient.measurement.unit.type }
    .map { ingredient.measurement.convert(it).scale(scale) }
    .filter { it.quantity.toFraction().roundToNearestFraction().reduce() > Fraction(1, 8) }

//  var selectedUnit by remember { mutableStateOf<MeasurementUnit?>(null) }

  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier
      .fillMaxWidth()
      .height(IntrinsicSize.Min),
  ) {
    // TODO: indicate that unit is forced

    Box(
      modifier = Modifier
        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
        .clip(RoundedCornerShape(percent = 25))
        .background(MaterialTheme.colorScheme.primary)
        .clickable(enabled = measurements.isNotEmpty()) { showBottomSheet = true },
      contentAlignment = Alignment.Center
    ) {
      val measurement = ingredient.measurement.scale(scale).run {
        vm.selectedUnits[ingredient]?.let { convert(it) } ?: normalize { it !is MeasurementUnit.FluidOunce }
      }
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

  val sheetState = rememberModalBottomSheetState()
  val scope = rememberCoroutineScope()

  if (showBottomSheet) {
    UnitSelectionBottomSheet(
      onDismissRequest = { showBottomSheet = false },
      onUnitSelect = {
//        selectedUnit = it.takeIf { selectedUnit != it }
        vm.selectedUnits[ingredient] = it.takeIf { vm.selectedUnits[ingredient] != it }

        scope.launch { sheetState.hide() }.invokeOnCompletion {
          if (!sheetState.isVisible) {
            showBottomSheet = false
          }
        }
      },
      ingredient = ingredient,
      measurements = measurements,
      selectedUnit = vm.selectedUnits[ingredient],
      sheetState = sheetState
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
  val vm = getViewModel<RecipePageViewModel>()

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

          // TODO: indicate selected measurement unit

          Box(
            modifier = Modifier
              .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
              .clip(RoundedCornerShape(percent = 25))
              .background(MaterialTheme.colorScheme.primary)
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
fun InstructionsList(
  instructions: List<Instruction>,
  scale: Double,
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
        scale = scale
      )

      if (instruction != instructions.last()) {
        HorizontalDivider()
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun InstructionComponent(
  instruction: Instruction,
  scale: Double,
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
          val vm = getViewModel<RecipePageViewModel>()

          var showBottomSheet by remember { mutableStateOf(false) }

          val measurements = MeasurementUnit.values
            .filter { it.type == ingredient.measurement.unit.type }
            .map { ingredient.measurement.convert(it).scale(scale) }
            .filter { it.quantity.toFraction().roundToNearestFraction().reduce() > Fraction(1, 8) }

//          var selectedUnit by remember { mutableStateOf<MeasurementUnit?>(null) }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
              .width(IntrinsicSize.Max)
              .height(IntrinsicSize.Max)
              .clip(CircleShape)
              .border(width = 2.dp, color = MaterialTheme.colorScheme.primary, shape = CircleShape)
              .clickable { showBottomSheet = true },
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            ) {
              val measurement = ingredient.measurement.scale(scale).run {
                vm.selectedUnits[ingredient]?.let { convert(it) } ?: normalize { it !is MeasurementUnit.FluidOunce }
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
                vm.selectedUnits[ingredient] = it.takeIf { vm.selectedUnits[ingredient] != it }
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                  if (!sheetState.isVisible) {
                    showBottomSheet = false
                  }
                }
              },
              ingredient = ingredient,
              measurements = measurements,
              selectedUnit = vm.selectedUnits[ingredient],
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
// PREVIEWS
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////

@Preview
@Composable
fun RecipePagePreview() {
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

  val instructions = listOf(
    Instruction(
      text = "Cook pasta in a pot of salted boiling water until al dente",
      ingredients = listOf(
        Ingredient(
          "Pasta",
          IngredientType.Dry,
          measurement = Measurement(8.0, MeasurementUnit.Ounce)
        )
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

  val recipe = Recipe(
    name = "Creamy Garlic Pasta Shells",
    ingredients = ingredients,
    instructions = instructions,
    equipment = emptyList(),
    servings = 4,
    description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
    time = RecipeTime(15, 15),
    source = RecipeSource("My Brain", "My Brain"),
    notes = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
  )


  SkilletAppTheme {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      RecipePage(recipe)
    }
  }
}

@Preview(showBackground = true, device = "spec:parent=pixel_5")
@Composable
fun IngredientsListEmptyPreview() {
  val ingredients = emptyList<Ingredient>()

  SkilletAppTheme {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      IngredientsList(ingredients = ingredients, scale = 1.0)
    }
  }
}

@Preview
@Composable
fun IngredientListPreview() {
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
    IngredientsList(ingredients = ingredients, scale = 1.0)
  }

}

@Preview(showBackground = true)
@Composable
fun IngredientComponentPreview() {
  val ingredient =
    Ingredient("Mini Shells Pasta", IngredientType.Dry, measurement = Measurement(8.0, MeasurementUnit.Ounce))

  Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    IngredientComponent(ingredient = ingredient, scale = 1.0)
  }
}

@Preview
@Composable
fun InstructionsListEmptyPreview() {
  val instructions = listOf(
    Instruction("Cook pasta in a pot of salted boiling water until al dente"),
    Instruction("Return pot to stove over medium heat then ass butter and olive oil. Once melted, add garlic then saute until light golden brown, about 30 seconds, being very careful not to burn. Sprinkle in flour then whisk and saute for 1 minute. Slowly pour in chicken broth and milk while whisking until mixture is smooth. Season with salt and pepper then switch to a wooden spoon and stir constantly until mixture is thick and bubbly, 4.-5 minutes."),
    Instruction("Remove pot from heat then stir in parmesan cheese, garlic powder, and parsley flakes until smooth. Add cooked pasta then stir to combine. Taste then adjust salt and pepper if necessary, and then serve."),
  )

  SkilletAppTheme {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      InstructionsList(instructions = emptyList(), scale = 1.0)
    }
  }
}

@Preview
@Composable
fun InstructionsListPreview() {
  val instructions = listOf(
    Instruction(
      text = "Cook pasta in a pot of salted boiling water until al dente",
      ingredients = listOf(
        Ingredient(
          "Pasta",
          IngredientType.Dry,
          measurement = Measurement(8.0, MeasurementUnit.Ounce)
        )
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
      InstructionsList(instructions = instructions, scale = 1.0)
    }
  }
}

@Preview
@Composable
fun InstructionComponentPreview() {
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
        InstructionComponent(instruction = instruction, scale = 1.0)
      }
    }
  }
}