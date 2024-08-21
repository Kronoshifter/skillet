package com.kronos.skilletapp.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kronos.skilletapp.utils.applyIf
import kotlinx.serialization.json.JsonNull.content

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
    modifier = modifier.clickable(enabled = enabled, onClick = onClick)
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
          },
        contentAlignment = Alignment.Center,
        content = detail
      )
    } else {
      Spacer(modifier = Modifier.size(boxSize))
    }

    content()
  }
}

@Composable
fun ItemPill(
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  onClick: () -> Unit = {},
  color: Color = MaterialTheme.colorScheme.primary,
  borderColor: Color = MaterialTheme.colorScheme.primary,
  label: @Composable RowScope.() -> Unit,
  content: @Composable () -> Unit
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = modifier
      .width(IntrinsicSize.Max)
      .height(IntrinsicSize.Max)
      .clip(CircleShape)
      .border(width = 2.dp, color = borderColor, shape = CircleShape)
      .clickable(enabled = enabled, onClick = onClick),
  ) {
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimary) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
          .clip(CircleShape)
          .background(color),
        content = label
      )
    }

    content()
  }
}