package com.kronos.skilletapp.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.Measurement
import com.kronos.skilletapp.model.MeasurementSystem
import com.kronos.skilletapp.model.MeasurementUnit
import com.kronos.skilletapp.ui.theme.SkilletAppTheme
import com.kronos.skilletapp.utils.Fraction
import com.kronos.skilletapp.utils.applyIf
import com.kronos.skilletapp.utils.toFraction
import kotlinx.coroutines.launch

@Composable
fun IngredientRow(
  ingredient: Ingredient,
  scale: Double = 1.0,
  selectedUnit: MeasurementUnit? = null,
  enabled: Boolean = true,
  onClick: () -> Unit = {},
) {
  val measurement = with(ingredient.measurement.scale(scale)) {
    selectedUnit?.let { convert(it) } ?: normalize { it !is MeasurementUnit.FluidOunce }
  }

  ItemRow(
    modifier = Modifier
      .fillMaxWidth()
      .height(IntrinsicSize.Min),
    showDetail = measurement.quantity > 0,
    detail = {
      val quantity = when (measurement.unit.system) {
        MeasurementSystem.Metric -> measurement.quantity.toString().take(4).removeSuffix(".")
        else -> measurement.quantity.toFraction().roundToNearestFraction().reduce().toDisplayString()
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
          modifier = Modifier
            .applyIf(measurement.unit !is MeasurementUnit.None) { offset(y = 4.dp) }
        )

        if (measurement.unit !is MeasurementUnit.None) {
          Text(
            text = measurement.unit.abbreviation,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 12.sp
          )
        }
      }
    },
    decoration = selectedUnit != null,
    enabled = enabled,
    onClick = onClick
  ) {
    Column {
      Text(
        text = ingredient.name.lowercase(),
        fontWeight = FontWeight.Bold
      )

      ingredient.comment?.let {
        Text(
          text = it.lowercase(),
          color = MaterialTheme.colorScheme.secondary,
          fontSize = 14.sp
        )
      }
    }
  }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun IngredientPill(
  ingredient: Ingredient,
  scale: Double = 1.0,
  selectedUnits: Map<Ingredient, MeasurementUnit?> = mapOf(),
  onUnitSelect: (Ingredient, MeasurementUnit?) -> Unit = { _, _ -> },
) {
  var showBottomSheet by remember { mutableStateOf(false) }

  val measurements = MeasurementUnit.values
    .filter { it.type == ingredient.measurement.unit.type }
    .map { ingredient.measurement.convert(it).scale(scale) }
    .filter { it.quantity.toFraction().roundToNearestFraction().reduce() > Fraction(1, 8) }

  val selectedUnit = selectedUnits[ingredient]

  val borderColor =
    selectedUnit?.let { MaterialTheme.colorScheme.onSecondaryContainer } ?: MaterialTheme.colorScheme.primary

  ItemPill(
    enabled = measurements.isNotEmpty(),
    onClick = { showBottomSheet = true },
    borderColor = borderColor,
    leadingContent = {
      val measurement = with(ingredient.measurement.scale(scale)) {
        selectedUnit?.let { convert(it) } ?: normalize { it !is MeasurementUnit.FluidOunce }
      }

      val quantity = when (measurement.unit.system) {
        MeasurementSystem.Metric -> measurement.quantity.toString().take(4).removeSuffix(".")
        else -> measurement.quantity.toFraction().roundToNearestFraction().reduce().toDisplayString()
      }

      Text(
        text = "$quantity ${measurement.unit.abbreviation}",
        color = MaterialTheme.colorScheme.onPrimary,
        fontSize = 18.sp,
        modifier = Modifier.padding(8.dp)
      )
    }
  ) {
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


@Preview
@Composable
private fun IngredientRowPreview() {
  val ingredient = Ingredient(
    name = "Pasta",
    measurement = Measurement(8.0, MeasurementUnit.Ounce),
    raw = "8 oz Pasta",
  )
  SkilletAppTheme {
    Surface {
      IngredientRow(
        ingredient = ingredient,
        scale = 1.0,
      )
    }
  }
}

@Preview
@Composable
private fun IngredientPillPreview() {
  val ingredient = Ingredient(
    name = "Pasta",
    measurement = Measurement(8.0, MeasurementUnit.Ounce),
    raw = "8 oz Pasta",
  )

  SkilletAppTheme {
    Surface {
      IngredientPill(
        ingredient = ingredient,
        scale = 1.0,
      )
    }
  }
}