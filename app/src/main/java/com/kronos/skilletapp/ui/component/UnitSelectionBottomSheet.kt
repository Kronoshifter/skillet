package com.kronos.skilletapp.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.Measurement
import com.kronos.skilletapp.model.MeasurementSystem
import com.kronos.skilletapp.model.MeasurementUnit
import com.kronos.skilletapp.utils.applyIf
import com.kronos.skilletapp.utils.toFraction

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun UnitSelectionBottomSheet(
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
                text = measurement.unit.aliases.first(),
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