package com.kronos.skilletapp.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.Measurement
import com.kronos.skilletapp.model.MeasurementUnit
import com.kronos.skilletapp.ui.theme.SkilletAppTheme
import com.kronos.skilletapp.utils.Fraction
import com.kronos.skilletapp.utils.modifier.applyIf
import com.kronos.skilletapp.utils.modifier.applyUnless
import com.kronos.skilletapp.utils.toFraction
import kotlinx.coroutines.launch

@Composable
fun IngredientRow(
  ingredient: Ingredient,
  modifier: Modifier = Modifier,
  scale: Double = 1.0,
  selectedUnit: MeasurementUnit? = null,
  enabled: Boolean = true,
  onClick: () -> Unit = {},
  trailingIcon: @Composable (() -> Unit)? = null,
) {
  val measurement = with(ingredient.measurement.scale(scale)) {
    selectedUnit?.let { convert(it) } ?: normalize { it !is MeasurementUnit.FluidOunce }
  }

  ItemRow(
    modifier = Modifier
      .fillMaxWidth()
      .height(IntrinsicSize.Min)
      .then(modifier),
    showDetail = measurement.quantity > 0,
    detail = {
      val quantity = measurement.displayQuantity

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
            .applyUnless(measurement.unit is MeasurementUnit.None) {
              offset(y = 4.dp)
            }
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
    onClick = onClick,
    trailingIcon = trailingIcon
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
//  selectedUnits: Map<Ingredient, MeasurementUnit?> = mapOf(),
  selectedUnit: MeasurementUnit? = null,
  onUnitSelect: (Ingredient, MeasurementUnit?) -> Unit = { _, _ -> },
) {
  var showBottomSheet by remember { mutableStateOf(false) }

  val measurements = MeasurementUnit.values
    .filter { it.type == ingredient.measurement.unit.type }
    .map { ingredient.measurement.convert(it).scale(scale) }
    .filter { it.quantity.toFraction().roundToNearestFraction().reduce() > Fraction(1, 8) }

  val borderColor =
    selectedUnit?.let { MaterialTheme.colorScheme.onSecondaryContainer } ?: MaterialTheme.colorScheme.primary

  ItemPill(
    enabled = measurements.isNotEmpty(),
    onClick = { showBottomSheet = true },
    borderColor = borderColor,
    leadingContent = {
      if (ingredient.measurement.quantity > 0) {
        val measurement = with(ingredient.measurement.scale(scale)) {
          selectedUnit?.let { convert(it) } ?: normalize { it !is MeasurementUnit.FluidOunce }
        }

        val quantity = measurement.displayQuantity.let {
          if (measurement.unit !is MeasurementUnit.None) {
            "$it ${measurement.unit.abbreviation}"
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
    }
  ) {
    Text(
      text = ingredient.name,
      modifier = Modifier
        .applyIf(ingredient.measurement.quantity <= 0) {
          padding(start = 8.dp)
        }
        .padding(vertical = 8.dp)
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
        trailingIcon = {
          IconButton(onClick = {}) {
            Icon(imageVector = Icons.Default.DragHandle, contentDescription = "Dragging")
          }
        }
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

@Preview
@Composable
fun IngredientPillNoQuantityPreview() {
  val ingredient = Ingredient(
    name = "Salt",
    measurement = Measurement(0.0, MeasurementUnit.None),
    raw = "Salt, to taste",
    comment = "to taste"
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

@Preview
@Composable
fun IngredientPillNoMeasurementPreview() {
  val ingredient = Ingredient(
    name = "Pepe",
    measurement = Measurement(1.0, MeasurementUnit.None),
    raw = "Pepe",
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