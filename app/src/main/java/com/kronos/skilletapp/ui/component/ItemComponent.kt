package com.kronos.skilletapp.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kronos.skilletapp.utils.applyIf

@Composable
fun ItemRow(
  modifier: Modifier = Modifier,
  showDetail: Boolean,
  detail: @Composable BoxScope.() -> Unit,
  decoration: Boolean = false,
  enabled: Boolean = true,
  onClick: () -> Unit = {},
  content: @Composable () -> Unit,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = modifier
  ) {
    val boxSize = 56.dp

    if (showDetail) {
      Box(
        modifier = Modifier
          .sizeIn(minWidth = boxSize, minHeight = boxSize)
          .clip(MaterialTheme.shapes.medium)
          .background(MaterialTheme.colorScheme.primary)
          .applyIf(decoration) {
            border(2.dp, MaterialTheme.colorScheme.onSecondaryContainer, MaterialTheme.shapes.medium)
          }
          .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
        content = detail
      )
    } else {
      Spacer(modifier = Modifier.size(boxSize))
    }

    content()
  }
}