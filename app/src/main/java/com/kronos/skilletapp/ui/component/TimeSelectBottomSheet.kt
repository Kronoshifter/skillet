package com.kronos.skilletapp.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSelectBottomSheet(
  initialTime: Int,
  sheetState: SheetState = rememberModalBottomSheetState(),
  onDismissRequest: () -> Unit,
  onTimeSelect: (Int) -> Unit,
  title: @Composable () -> Unit,
) {
  ModalBottomSheet(
    sheetState = sheetState,
    onDismissRequest = onDismissRequest
  ) {
    var time by remember { mutableIntStateOf(initialTime) }

    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
    ) {
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .fillMaxWidth()
      ) {
        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.titleLarge) {
          title()
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(onClick = { onTimeSelect(time) }) {
          Text(text = "Save")
        }
      }

      Row(modifier = Modifier.fillMaxWidth()) {
        val hours = time / 60
        val hoursOptions = (0..23).toList()

        val minutes = time % 60
        val minutesOptions = (0..55 step 5).toList()

        InfiniteScrollingPicker(
          options = hoursOptions,
          selected = hours,
          onSelect = { time = it * 60 + minutes },
          modifier = Modifier.weight(1f)
        ) {
          Text(text = if (it > 0) "$it hours" else "-")
        }

        InfiniteScrollingPicker(
          options = minutesOptions,
          selected = minutes,
          onSelect = { time = hours * 60 + it },
          modifier = Modifier.weight(1f)
        ) {
          Text(text = if (it > 0) "$it minutes" else "-")
        }
      }
    }
  }
}