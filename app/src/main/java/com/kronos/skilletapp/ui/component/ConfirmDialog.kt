package com.kronos.skilletapp.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun ConfirmDialog(
  onDismissRequest: () -> Unit,
  onConfirm: () -> Unit,
  modifier: Modifier = Modifier,
  title: String? = null,
  text: String? = null,
  icon: ImageVector? = null,
  onDismiss: (() -> Unit)? = null,
) {
  AlertDialog(
    onDismissRequest = onDismissRequest,
    confirmButton = {
      TextButton(onClick = onConfirm) {
        Text("Confirm")
      }
    },
    dismissButton = {
      onDismiss?.let {
        TextButton(onClick = it) {
          Text("Cancel")
        }
      }
    },
    modifier = modifier,
    title = { title?.let { Text(it) } },
    text = { text?.let { Text(it) } },
    icon = { icon?.let { Icon(it, contentDescription = "Dialog Icon") } }
  )
}