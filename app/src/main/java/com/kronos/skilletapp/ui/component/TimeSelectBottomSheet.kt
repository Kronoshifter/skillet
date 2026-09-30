package com.kronos.skilletapp.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kronos.skilletapp.utils.pluralize

private const val MINUTES_PER_HOUR = 60

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSelectBottomSheet(
  initialTime: Int,
  sheetState: SheetState = rememberModalBottomSheetState(),
  onDismissRequest: () -> Unit,
  onTimeSelect: (Int) -> Unit,
  title: @Composable () -> Unit,
) {
  var hours by remember { mutableIntStateOf(initialTime / MINUTES_PER_HOUR) }
  var minutes by remember { mutableIntStateOf(initialTime % MINUTES_PER_HOUR) }

  ActionBottomSheet(
    sheetState = sheetState,
    onDismissRequest = onDismissRequest,
    modifier = Modifier.fillMaxWidth().padding(8.dp),
    title = title,
    action = { TextButton(onClick = { onTimeSelect(hours * MINUTES_PER_HOUR + minutes) }) { Text(text = "Save") } },
  ) {
    Row(modifier = Modifier.fillMaxWidth()) {
      val hoursOptions = (0..23).toList()
      val minutesOptions = (0..55 step 5).toList()

      InfiniteScrollingPicker(
        options = hoursOptions,
        selected = hours,
        onSelect = { hours = it },
        modifier = Modifier.weight(1f),
      ) { i ->
        Text(text = if (i > 0) "$i hour".pluralize(i) { "${it}s" } else "-")
      }

      InfiniteScrollingPicker(
        options = minutesOptions,
        selected = minutes,
        onSelect = { minutes = it },
        modifier = Modifier.weight(1f),
      ) { i ->
        Text(text = if (i > 0) "$i minute".pluralize(i) { "${it}s" } else "-")
      }
    }
  }
}
