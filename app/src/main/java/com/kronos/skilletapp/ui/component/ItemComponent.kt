package com.kronos.skilletapp.ui.component

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kronos.skilletapp.ui.theme.SkilletAppTheme
import com.kronos.skilletapp.utils.modifier.applyIf

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemRow(
  modifier: Modifier = Modifier,
  showDetail: Boolean,
  detail: @Composable BoxScope.() -> Unit,
  decoration: Boolean = false,
  enabled: Boolean = true,
  onClick: () -> Unit = {},
  onLongClick: () -> Unit = {},
  trailingIcon: @Composable (() -> Unit)? = null,
  content: @Composable () -> Unit,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = modifier
      .combinedClickable(
        enabled = enabled,
        onClick = onClick,
        onLongClick = onLongClick
      )
  ) {
    val boxSize = 56.dp

    AnimatedContent(
      targetState = showDetail,
      label = "Show Detail",
      transitionSpec = {
        fadeIn(animationSpec = tween(durationMillis = 220, delayMillis = 120)) togetherWith
        fadeOut(animationSpec = tween(durationMillis = 90))
      }
    ) { showRowDetail ->
      if (showRowDetail) {
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
    }


    Box(modifier = Modifier.weight(1f),) {
      content()
    }

    trailingIcon?.invoke()
  }
}

@Composable
fun ItemPill(
  modifier: Modifier = Modifier,
  enabled: Boolean = false,
  onClick: () -> Unit = {},
  color: Color = MaterialTheme.colorScheme.primary,
  borderColor: Color = MaterialTheme.colorScheme.primary,
  leadingContent: @Composable RowScope.() -> Unit,
  trailingIcon: @Composable (() -> Unit)? = null,
  content: @Composable () -> Unit
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = modifier
      .width(IntrinsicSize.Max)
      .height(IntrinsicSize.Min)
      .clip(CircleShape)
      .border(width = 2.dp, color = borderColor, shape = CircleShape)
      .clickable(enabled = enabled, onClick = onClick),
  ) {
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimary) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
          .fillMaxHeight()
          .clip(CircleShape)
          .background(color),
        content = leadingContent
      )
    }

    Box(modifier = Modifier.weight(1f),) {
      content()
    }

    trailingIcon?.invoke() ?: Spacer(modifier = Modifier.width(8.dp))
  }
}

@Preview
@Composable
fun ItemPillPreview() {
  SkilletAppTheme {
    Surface {
      ItemPill(
        leadingContent = {
          Text(
            text = "Pasta",
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 18.sp,
            modifier = Modifier.padding(8.dp)
          )
        },
        trailingIcon = { IconButton(onClick = {}) { Icon(Icons.Filled.Clear, contentDescription = null) } },
      ) {
        Text(text = "Pasta")
      }
    }
  }
}