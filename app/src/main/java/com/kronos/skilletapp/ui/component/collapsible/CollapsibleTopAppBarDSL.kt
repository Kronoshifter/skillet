@file:OptIn(ExperimentalMaterial3Api::class)

package com.kronos.skilletapp.ui.component.collapsible

import androidx.compose.foundation.background
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMaxBy
import androidx.compose.ui.util.fastMinByOrNull
import androidx.compose.ui.util.lerp
import kotlin.math.roundToInt

class CollapsibleTopAppBarLayoutMeasurePolicy(
  private val appBarState: TopAppBarState,
) : MeasurePolicy {
  override fun MeasureScope.measure(measurables: List<Measurable>, constraints: Constraints): MeasureResult {
    val placeables = measurables.fastMap {
      it.measure(
        constraints.copy(
          minWidth = 0,
          minHeight = 0,
          maxHeight = Constraints.Infinity,
        )
      )
    }

    val minHeight = placeables.fastMinByOrNull { it.height }?.height?.coerceIn(constraints.minHeight, constraints.maxHeight) ?: 0
    val maxHeight = placeables.fastMaxBy { it.height }?.height?.coerceIn(constraints.minHeight, constraints.maxHeight) ?: 0
    val maxWidth = placeables.fastMaxBy { it.width }?.width?.coerceIn(constraints.minWidth, constraints.maxWidth) ?: 0

    appBarState.heightOffsetLimit = (minHeight - maxHeight).toFloat()

    val heightOffset = appBarState.heightOffset.roundToInt()
    val layoutHeight = maxHeight + heightOffset

    return layout(maxWidth, layoutHeight) {
      val progress = appBarState.progress

      placeables.forEachIndexed { index, placeable ->
        placeable.placeCollapsible(maxWidth, layoutHeight, progress, appBarState.heightOffset)
      }
    }
  }
}

context(measureScope: MeasureScope, placementScope: Placeable.PlacementScope)
fun Placeable.placeCollapsible(maxWidth: Int, layoutHeight: Int, progress: Float, heightOffset: Float) = with(placementScope) {
  roadData?.let { (collapsed, expanded) ->
    val collapsedOffset = collapsed.align(
      size = IntSize(width, height),
      space = IntSize(maxWidth, layoutHeight),
      layoutDirection = measureScope.layoutDirection,
    )

    val expandedOffset = expanded.align(
      size = IntSize(width, height),
      space = IntSize(maxWidth, layoutHeight),
      layoutDirection = measureScope.layoutDirection,
    )

    val offset = lerp(collapsedOffset, expandedOffset, progress)

    place(offset)
  } ?: horizontalRoadData?.let { (collapsed, expanded) ->
    val collapsedOffset = collapsed.align(
      size = width,
      space = maxWidth,
      layoutDirection = measureScope.layoutDirection,
    )

    val expandedOffset = expanded.align(
      size = width,
      space = maxWidth,
      layoutDirection = measureScope.layoutDirection,
    )

    val offset = lerp(collapsedOffset, expandedOffset, progress)

    place(offset, 0)
  } ?: verticalRoadData?.let { (collapsed, expanded) ->
    val collapsedOffset = collapsed.align(
      size = height,
      space = layoutHeight,
    )

    val expandedOffset = expanded.align(
      size = height,
      space = layoutHeight,
    )

    val offset = lerp(collapsedOffset, expandedOffset, progress)

    place(0, offset)
  } ?: parallaxRatio?.let { parallaxRatio ->
    val offset = (parallaxRatio * heightOffset).roundToInt()

    placeRelative(0, offset)
  } ?: placeRelative(0, 0)
}

private enum class CollapsibleTopAppBarLayoutContent {
  Title, NavigationIcon, Actions, MainContent, Background
}

interface CollapsibleTopAppBarScope {
  val progress: Float
  val heightOffset: Float

  fun Modifier.fadeOnExpand(targetAlpha: Float = 0f): Modifier = this then Modifier.fade(1f, targetAlpha)
  fun Modifier.fadeOnCollapse(targetAlpha: Float = 0f): Modifier = this then Modifier.fade(targetAlpha, 1f)
  fun Modifier.fade(collapsed: Float, expanded: Float): Modifier
  fun Modifier.background(collapsedColor: Color, expandedColor: Color, collapsedShape: Shape = RectangleShape, expandedShape: Shape = collapsedShape): Modifier
}

class CollapsibleTopAppBarScopeImpl(
  private val state: TopAppBarState,
) : CollapsibleTopAppBarScope {
  override val progress
    get() = state.progress

  override val heightOffset
    get() = state.heightOffset

  override fun Modifier.fade(collapsed: Float, expanded: Float): Modifier {
    val alpha = lerp(collapsed, expanded, progress)
    return this then Modifier.alpha(alpha = alpha)
  }

  override fun Modifier.background(collapsedColor: Color, expandedColor: Color, collapsedShape: Shape, expandedShape: Shape): Modifier {
    val color = lerp(collapsedColor, expandedColor, progress)
    return this then Modifier.background(color = color)
  }
}

val TopAppBarState.progress: Float
  get() = 1 - collapsedFraction
