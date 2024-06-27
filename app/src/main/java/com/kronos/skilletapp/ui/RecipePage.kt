package com.kronos.skilletapp.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kronos.skilletapp.model.*
import com.kronos.skilletapp.ui.theme.SkilletAppTheme
import com.kronos.skilletapp.utils.toFraction

object RecipePage {
  const val INGREDIENTS = 0
  const val INSTRUCTIONS = 1
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RecipePage(recipe: Recipe) {
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

        RecipeControls(
          scale = scale,
          servings = servings,
          baseServings = recipe.servings,
          onServingsChanged = { servings = it },
          onScaleChanged = { scale = it }
        )

        LaunchedEffect(scale) {
          servings = (scale * recipe.servings).toInt()
        }

        LaunchedEffect(servings) {
          scale = servings / recipe.servings.toDouble()
        }

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
fun RecipeControls(
  scale: Double,
  servings: Int,
  baseServings: Int,
  onServingsChanged: (servings: Int) -> Unit,
  onScaleChanged: (scale: Double) -> Unit,
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
    OutlinedIconButton(
      onClick = { onServingsChanged((servings - 1).coerceAtLeast(1)) }
    ) {
      Icon(imageVector = Icons.Filled.Remove, contentDescription = null)
    }
    Text(text = "$servings servings")

    OutlinedIconButton(
      onClick = { onServingsChanged(servings + 1) }
    ) {
      Icon(imageVector = Icons.Filled.Add, contentDescription = null)
    }

    SingleChoiceSegmentedButtonRow(
      modifier = Modifier.width(IntrinsicSize.Min)
    ) {
      scaleOptions.forEach { option ->
        val selected = scale == option.toDouble()
        SegmentedButton(
          selected = selected,
          onClick = { onScaleChanged(option.toDouble()) },
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

@Composable
fun IngredientComponent(
  ingredient: Ingredient,
  scale: Double,
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
        .clickable { /*TODO: implement ingredient box click*/ },
      contentAlignment = Alignment.Center
    ) {
      val measurement = ingredient.measurement.scale(scale).normalize { it !is MeasurementUnit.FluidOunce }
      val quantity = measurement.quantity.toFraction().roundToNearestFraction().reduce().toString()

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
fun InstructionsList(
  instructions: List<Instruction>,
  scale: Double
) {
  Text(text = "Instructions")
}

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

  val recipe = Recipe(
    name = "Creamy Garlic Pasta Shells",
    ingredients = ingredients,
    instructions = emptyList(),
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