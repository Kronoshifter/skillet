package com.kronos.skilletapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.michaelbull.result.combine
import com.kronos.skilletapp.Greeting
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.Measurement
import com.kronos.skilletapp.model.MeasurementUnit
import com.kronos.skilletapp.ui.theme.SkilletAppTheme
import com.kronos.skilletapp.utils.roundToEighth
import com.kronos.skilletapp.utils.toFraction
import java.text.DecimalFormat

@Composable
fun RecipePage() {
  SkilletAppTheme {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {

    }
  }
}

@Composable
fun IngredientList(ingredients: List<Ingredient>) {
  if (ingredients.isEmpty()) {
    Text(text = "No Ingredients")
    return
  }

  Column {
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(8.dp)
    ) {
      Text(text = "Scale")
    }

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      items(ingredients) { ingredient ->
        IngredientComponent(ingredient = ingredient)
      }
    }
  }
}

@Composable
fun IngredientComponent(ingredient: Ingredient) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Box(
      modifier = Modifier
        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primary),
      contentAlignment = Alignment.Center
    ) {
      val quantity = ingredient.measurement.quantity.toFraction().roundToNearestFraction().reduce().toString()

      Text(
        text = quantity,
        color = MaterialTheme.colorScheme.onPrimary,
        fontSize = 18.sp,
        modifier = Modifier.align(Alignment.Center)
      )
    }

    Text(
      text = ingredient.measurement.unit.abbreviation,
      fontWeight = FontWeight.Bold
    )
    Text(text = ingredient.name.lowercase())
  }
}


@Preview(showBackground = true, device = "spec:parent=pixel_5")
@Composable
fun RecipePagePreview() {
  RecipePage()
}

@Preview
@Composable
fun IngredientListPreview() {
  val ingredients = listOf(
    Ingredient("Mini Shells Pasta", "pasta", Measurement(8.0, MeasurementUnit.Ounce)),
    Ingredient("Olive Oil", "Oil", Measurement(1.0, MeasurementUnit.Tablespoon)),
    Ingredient("Butter", "Butter", Measurement(1.0, MeasurementUnit.Tablespoon)),
    Ingredient("Garlic", "Garlic", Measurement(2.0, MeasurementUnit.Custom("clove", "clove"))),
    Ingredient("Flour", "Flour", Measurement(2.0, MeasurementUnit.Tablespoon)),
    Ingredient("Chicken Broth", "Chicken Broth", Measurement(0.75, MeasurementUnit.Cup)),
    Ingredient("Milk", "Milk", Measurement(2.5, MeasurementUnit.Cup)),
  )

  Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    IngredientList(ingredients = ingredients)
  }

}

@Preview(widthDp = 400, heightDp = 800, showBackground = true, device = "spec:parent=pixel_5")
@Composable
fun IngredientComponentPreview() {
  val ingredient = Ingredient("Mini Shells Pasta", "pasta", Measurement(8.0, MeasurementUnit.Ounce))

  Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    IngredientComponent(ingredient = ingredient)
  }
}