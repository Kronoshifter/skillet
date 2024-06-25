package com.kronos.skilletapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.util.rangeTo
import com.github.michaelbull.result.combine
import com.kronos.skilletapp.Greeting
import com.kronos.skilletapp.model.*
import com.kronos.skilletapp.ui.component.SegmentedButton
import com.kronos.skilletapp.ui.theme.SkilletAppTheme
import com.kronos.skilletapp.utils.roundToEighth
import com.kronos.skilletapp.utils.toFraction
import java.text.DecimalFormat
import kotlin.math.roundToInt

@Composable
fun RecipePage(
  recipe: Recipe
) {
  SkilletAppTheme {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      Column {
        val scaleOptions = (1..3)
        var scale by remember { mutableDoubleStateOf(scaleOptions.first().toDouble()) }
        var servings by remember { mutableIntStateOf(recipe.servings) }

        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
        ) {
          OutlinedIconButton(
            onClick = {
              servings = (servings - 1).coerceAtLeast(1)
              scale = servings / recipe.servings.toDouble()
            }
          ) {
            Icon(imageVector = Icons.Filled.Remove, contentDescription = null)
          }
          Text(text = "$servings servings")

          OutlinedIconButton(
            onClick = {
              servings++
              scale = servings / recipe.servings.toDouble()
            }
          ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = null)
          }

          SegmentedButton(
            options = {
              scaleOptions.forEach { option ->
                segment(
                  option = option,
                  label = "${option}x",
                  selected = scale == option.toDouble(),
                )
              }
            },
            onSelectedChanged = {
              scale = it.toDouble()
              servings = recipe.servings * it
            },
            modifier = Modifier.width(IntrinsicSize.Min)
          )
        }

        IngredientList(
          ingredients = recipe.ingredients,
          scale = scale
        )
      }
    }
  }
}

@Composable
fun IngredientList(
  ingredients: List<Ingredient>,
  scale: Double
) {
  if (ingredients.isEmpty()) {
    Text(text = "No Ingredients")
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
  scale: Double
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
        modifier = Modifier.align(Alignment.Center)
      ) {
        Text(
          text = quantity,
          color = MaterialTheme.colorScheme.onPrimary,
          fontSize = 18.sp
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


@Preview(showBackground = true, device = "spec:parent=pixel_5")
@Composable
fun RecipePagePreview() {
  val recipe = Recipe(
    name = "Creamy Garlic Pasta Shells",
    ingredients = listOf(
      Ingredient("Mini Shells Pasta", IngredientType.Dry, measurement = Measurement(8.0, MeasurementUnit.Ounce)),
      Ingredient("Olive Oil", IngredientType.Wet, measurement = Measurement(1.0, MeasurementUnit.Tablespoon)),
      Ingredient("Butter", IngredientType.Wet, measurement = Measurement(1.0, MeasurementUnit.Tablespoon)),
      Ingredient("Garlic", IngredientType.Dry, measurement = Measurement(2.0, MeasurementUnit.Custom("clove", "clove"))),
      Ingredient("Flour", IngredientType.Dry, measurement = Measurement(2.0, MeasurementUnit.Tablespoon)),
      Ingredient("Chicken Broth", IngredientType.Wet, measurement = Measurement(0.75, MeasurementUnit.Cup)),
      Ingredient("Milk", IngredientType.Wet, measurement = Measurement(2.5, MeasurementUnit.Cup)),
    ),
    instructions = emptyList(),
    equipment = emptyList(),
    servings = 4,
    description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
    time = RecipeTime(15, 15),
    source = RecipeSource("My Brain", "My Brain"),
    notes = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
  )
  RecipePage(recipe = recipe)
}

@Preview
@Composable
fun IngredientListPreview() {
  val ingredients = listOf(
    Ingredient("Mini Shells Pasta", IngredientType.Dry, measurement = Measurement(8.0, MeasurementUnit.Ounce)),
    Ingredient("Olive Oil", IngredientType.Wet, measurement = Measurement(1.0, MeasurementUnit.Tablespoon)),
    Ingredient("Butter", IngredientType.Wet, measurement = Measurement(1.0, MeasurementUnit.Tablespoon)),
    Ingredient("Garlic", IngredientType.Dry, measurement = Measurement(2.0, MeasurementUnit.Custom("clove", "clove"))),
    Ingredient("Flour", IngredientType.Dry, measurement = Measurement(2.0, MeasurementUnit.Tablespoon)),
    Ingredient("Chicken Broth", IngredientType.Wet, measurement = Measurement(0.75, MeasurementUnit.Cup)),
    Ingredient("Milk", IngredientType.Wet, measurement = Measurement(2.5, MeasurementUnit.Cup)),
  )

  Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    IngredientList(ingredients = ingredients, scale = 1.0)
  }

}

@Preview(widthDp = 400, heightDp = 800, showBackground = true, device = "spec:parent=pixel_5")
@Composable
fun IngredientComponentPreview() {
  val ingredient = Ingredient("Mini Shells Pasta", IngredientType.Dry, measurement = Measurement(8.0, MeasurementUnit.Ounce))

  Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    IngredientComponent(ingredient = ingredient, scale = 1.0)
  }
}