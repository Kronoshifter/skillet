package com.kronos.skilletapp.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.MeasurementSystem
import com.kronos.skilletapp.model.MeasurementUnit
import com.kronos.skilletapp.utils.Fraction
import com.kronos.skilletapp.utils.applyIf
import com.kronos.skilletapp.utils.toFraction
import kotlinx.coroutines.launch

@Composable
fun IngredientRow(
  ingredient: Ingredient,
  scale: Double,
  selectedUnit: MeasurementUnit? = null,
  enabled: Boolean = true,
  onClick: () -> Unit = {},
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier
      .fillMaxWidth()
      .height(IntrinsicSize.Min),
  ) {

    val measurement = with(ingredient.measurement.scale(scale)) {
      selectedUnit?.let { convert(it) } ?: normalize { it !is MeasurementUnit.FluidOunce }
    }

    val boxSize = 56.dp

    if (measurement.quantity > 0) {
      Box(
        modifier = Modifier
          .sizeIn(minWidth = boxSize, minHeight = boxSize)
          .clip(MaterialTheme.shapes.medium)
          .background(MaterialTheme.colorScheme.primary)
          .applyIf(selectedUnit != null) {
            border(2.dp, MaterialTheme.colorScheme.onSecondaryContainer, RoundedCornerShape(percent = 25))
          }
          .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
      ) {

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
            modifier = Modifier
              .applyIf(measurement.unit !is MeasurementUnit.None) { offset(y = 4.dp) }
          )

          if (measurement.unit !is MeasurementUnit.None) {
            Text(
              text = measurement.unit.aliases.first(),
              color = MaterialTheme.colorScheme.onPrimary,
              fontSize = 12.sp
            )
          }

        }
      }
    } else {
      Spacer(modifier = Modifier.size(boxSize))
    }

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
  scale: Double,
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
      val measurement = with(ingredient.measurement.scale(scale)) {
        selectedUnit?.let { convert(it) } ?: normalize { it !is MeasurementUnit.FluidOunce }
      }

      val quantity = when (measurement.unit.system) {
        MeasurementSystem.Metric -> measurement.quantity.toString().take(4).removeSuffix(".")
        else -> measurement.quantity.toFraction().roundToNearestFraction().reduce().toString()
      }

      Text(
        text = "$quantity ${measurement.unit.aliases}",
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