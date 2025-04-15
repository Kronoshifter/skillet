package com.kronos.skilletapp.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.measurement.Measurement
import com.kronos.skilletapp.model.measurement.MeasurementUnit
import com.kronos.skilletapp.utils.modifier.applyIf

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
          val quantity = measurement.displayQuantity

          val bgColor = MaterialTheme.colorScheme.primaryContainer
          val contentColor = contentColorFor(bgColor)

          Box(
            modifier = Modifier
              .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
              .clip(MaterialTheme.shapes.medium)
              .background(bgColor, MaterialTheme.shapes.medium)
              .applyIf(selectedUnit == measurement.unit) {
                border(
                  width = 2.dp,
                  color = contentColor,
                  shape = MaterialTheme.shapes.medium
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
                color = contentColor,
                fontSize = 18.sp,
                modifier = Modifier.offset(y = 4.dp)
              )

              Text(
                text = measurement.unit.abbreviation,
                color = contentColor,
                fontSize = 12.sp
              )
            }
          }
        }
      }
    }
  }
}