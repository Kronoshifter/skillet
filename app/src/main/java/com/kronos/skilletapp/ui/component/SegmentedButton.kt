@file:Suppress("WrapUnaryOperator")

package com.kronos.skilletapp.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
private fun <T> SegmentedButton(
  options: List<SegmentedButtonOption<T>>,
  onSelectedChanged: ((T) -> Unit)?,
  modifier: Modifier = Modifier,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(-1.dp),
    modifier = modifier.heightIn(max = 40.dp)
  ) {
    options.forEach { option ->
      SegmentedButtonSegment(
        option = option.option,
        selected = option.selected,
        onSelectedChanged = onSelectedChanged,
        icon = option.icon,
        shape = when (option) {
          options.first() -> RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50)
          options.last() -> RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50)
          else -> RectangleShape
        },
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
fun <T> SegmentedButton(
  options: SegmentedButtonScope<T>.() -> Unit,
  onSelectedChanged: ((T) -> Unit)?,
  modifier: Modifier = Modifier,
) = SegmentedButton(
  options = SegmentedButtonScopeImpl<T>().apply(options).options,
  onSelectedChanged = onSelectedChanged,
  modifier = modifier
)

@Composable
private fun <T> SegmentedButtonSegment(
  option: T,
  selected: Boolean,
  onSelectedChanged: ((T) -> Unit)?,
  modifier: Modifier = Modifier,
  shape: Shape = CircleShape,
  icon: @Composable (() -> Unit)? = null,
) {
  OutlinedButton(
    onClick = { onSelectedChanged?.invoke(option) },
    shape = shape,
    colors = ButtonDefaults.outlinedButtonColors(
      containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
    ),
    contentPadding = PaddingValues(horizontal = 16.dp),
    modifier = modifier
  ) {
    if (selected) {
      Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
    } else {
      CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
        icon?.invoke()
      }
    }

    Spacer(modifier = Modifier.width(8.dp))

    Text(
      text = option.toString(),
      color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
    )
  }
}

interface SegmentedButtonScope<T> {
  fun segment(
    option: T,
    selected: Boolean,
    icon: @Composable (() -> Unit)? = null,
  )
}

private class SegmentedButtonScopeImpl<T> : SegmentedButtonScope<T> {
  val options = mutableListOf<SegmentedButtonOption<T>>()

  override fun segment(
    option: T,
    selected: Boolean,
    icon: @Composable (() -> Unit)?,
  ) {
    options.add(SegmentedButtonOption(option, selected, icon))
  }
}

class SegmentedButtonOption<T>(
  val option: T,
  val selected: Boolean,
  val icon: @Composable (() -> Unit)? = null,
)

@Preview
@Composable
fun SegmentedButtonPreview() {
  var selectedOption by remember { mutableStateOf<String?>(null) }

  Surface {
    SegmentedButton(
      options = {
        segment(
          option = "Option 1",
          selected = selectedOption == "Option 1",
          icon = { Icon(imageVector = Icons.Filled.ShoppingCart, contentDescription = null) }
        )
        segment(
          option = "Option 2",
          selected = selectedOption == "Option 2",
          icon = { Icon(imageVector = Icons.Filled.Star, contentDescription = null) }
        )
        segment(
          option = "Option 3",
          selected = selectedOption == "Option 3",
        )
      },
      onSelectedChanged = { selectedOption = if (selectedOption != it) it else null }
    )
  }
}

@Preview
@Composable
fun MultiselectSegmentedButtonPreview() {
  val selection = remember { mutableStateListOf<String>() }

  Surface {
    SegmentedButton(
      options = {
        segment(
          option = "Option 1",
          selected = "Option 1" in selection,
          icon = { Icon(imageVector = Icons.Filled.ShoppingCart, contentDescription = null) }
        )
        segment(
          option = "Option 2",
          selected = "Option 2" in selection,
          icon = { Icon(imageVector = Icons.Filled.Star, contentDescription = null) }
        )
        segment(
          option = "Option 3",
          selected = "Option 3" in selection,
        )
      },
      onSelectedChanged = { if (it in selection) selection.remove(it) else selection.add(it) }
    )
  }
}