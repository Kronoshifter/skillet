package com.kronos.skilletapp.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kronos.skilletapp.utils.pluralize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSelectBottomSheet(
  initialTime: Int,
  sheetState: SheetState = rememberModalBottomSheetState(),
  onDismissRequest: () -> Unit,
  onTimeSelect: (Int) -> Unit,
  title: @Composable () -> Unit,
) {
  var hours by remember { mutableIntStateOf(initialTime / 60) }
  var minutes by remember { mutableIntStateOf(initialTime % 60) }

  ActionBottomSheet(
    sheetState = sheetState,
    onDismissRequest = onDismissRequest,
    modifier = Modifier
      .fillMaxWidth()
      .padding(8.dp),
    title = title,
    action = {
      TextButton(
        onClick = { onTimeSelect(hours * 60 + minutes) },
      ) {
        Text(text = "Save")
      }
    }
  ) {
    Row(modifier = Modifier.fillMaxWidth()) {
      val hoursOptions = (0..23).toList()
      val minutesOptions = (0..55 step 5).toList()

      InfiniteScrollingPicker(
        options = hoursOptions,
        selected = hours,
        onSelect = { hours = it },
        modifier = Modifier.weight(1f)
      ) { i ->
        Text(text = if (i > 0) "$i hour".pluralize(i) { "${it}s" } else "-")
      }

      InfiniteScrollingPicker(
        options = minutesOptions,
        selected = minutes,
        onSelect = { minutes = it },
        modifier = Modifier.weight(1f)
      ) { i ->
        Text(text = if (i > 0) "$i minute".pluralize(i) { "${it}s" } else "-")
      }
    }
  }
}
