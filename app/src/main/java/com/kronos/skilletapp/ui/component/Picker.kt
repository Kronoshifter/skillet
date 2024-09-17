package com.kronos.skilletapp.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.kronos.skilletapp.ui.DisableRipple
import com.kronos.skilletapp.ui.theme.SkilletAppTheme
import com.kronos.skilletapp.utils.modifier.verticalFadingEdge
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> InfiniteScrollingPicker(
  options: List<T>,
  selected: T,
  onSelect: (T) -> Unit,
  modifier: Modifier = Modifier,
  visibleItemCount: Int = 3,
  itemHeight: Dp = 40.dp,
  divider: @Composable (offset: Dp) -> Unit = {
    HorizontalDivider(
      modifier = Modifier.offset(y = it),
    )
  },
  optionContent: @Composable BoxScope.(T) -> Unit = { Text(text = it.toString()) },
) {
  require(visibleItemCount % 2 == 1) {
    "visibleItemCount must be an odd number"
  }

  val center = visibleItemCount / 2
  val scrollCount = Int.MAX_VALUE
  val scrollCenter = scrollCount / 2
  val startIndex = scrollCenter - (scrollCenter % options.size) - center + options.indexOf(selected)

  val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)
  val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
  val scope = rememberCoroutineScope()

  Box(modifier = modifier) {
    LazyColumn(
      horizontalAlignment = Alignment.CenterHorizontally,
      state = listState,
      flingBehavior = flingBehavior,
      modifier = Modifier
        .fillMaxWidth()
        .height(visibleItemCount * itemHeight)
        .verticalFadingEdge(),
    ) {
      items(scrollCount) { index -> 
        DisableRipple {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .height(itemHeight)
              .fillMaxWidth()
              .clickable {
                scope.launch { listState.animateScrollToItem(index = index - center) }
              }
          ) {
            optionContent(options[index % options.size])
          }
        }
      }
    }

    divider(offset = itemHeight * (center))
    divider(offset = itemHeight * (center + 1))
  }

  LaunchedEffect(listState) {
    snapshotFlow { listState.firstVisibleItemIndex }
      .map { index -> options[(index + center) % options.size] }
      .distinctUntilChanged()
      .collect { onSelect(it) }
  }
}

@Preview
@Composable
fun PickerPreview() {
  val options = 0..10
  var selected by remember { mutableStateOf(options.first()) }

  SkilletAppTheme {
    Surface {
      Column {
        InfiniteScrollingPicker(
          options = options.toList(),
          selected = selected,
          onSelect = { selected = it },
          visibleItemCount = 3
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Selected: $selected")
      }
    }
  }
}

@Preview
@Composable
fun DoublePickerPreview() {
  SkilletAppTheme {
    Surface {
      Column {
        val numberPickerOptions = (0..10).toList()
        var numberPickerSelected by remember { mutableIntStateOf(numberPickerOptions.first()) }

        val unitPickerOptions = listOf("Seconds", "Minutes", "Hours")
        var unitPickerSelected by remember { mutableStateOf(unitPickerOptions.first()) }

        Row(modifier = Modifier) {
          InfiniteScrollingPicker(
            options = numberPickerOptions,
            selected = numberPickerSelected,
            onSelect = { numberPickerSelected = it },
            modifier = Modifier.weight(1f)
          )

          InfiniteScrollingPicker(
            options = unitPickerOptions,
            selected = unitPickerSelected,
            onSelect = { unitPickerSelected = it },
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Selected: $numberPickerSelected $unitPickerSelected")
      }
    }
  }
}