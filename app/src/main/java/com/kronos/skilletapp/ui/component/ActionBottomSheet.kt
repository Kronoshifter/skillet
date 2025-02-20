package com.kronos.skilletapp.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionBottomSheet(
  sheetState: SheetState = rememberModalBottomSheetState(),
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  title: @Composable () -> Unit,
  action: @Composable () -> Unit,
  content: @Composable () -> Unit
) {
  ModalBottomSheet(
    sheetState = sheetState,
    onDismissRequest = onDismissRequest
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = modifier
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
      ) {
        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.titleLarge) {
          Box(
            modifier = Modifier.align(Alignment.Center)
          ) {
            title()
          }
        }

        Box(
          modifier = Modifier.align(Alignment.CenterEnd)
        ) {
          action()
        }
      }

      content()
    }
  }
}