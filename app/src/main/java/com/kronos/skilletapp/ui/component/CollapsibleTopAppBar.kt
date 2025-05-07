@file:OptIn(ExperimentalMaterial3Api::class)

package com.kronos.skilletapp.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.*
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ParentDataModifierNode
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.*
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMaxBy
import androidx.compose.ui.util.fastMinByOrNull
import androidx.compose.ui.util.lerp
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun CollapsibleTopAppBar(
  title: @Composable () -> Unit,
  scrollBehavior: TopAppBarScrollBehavior,
  modifier: Modifier = Modifier,
  navigationIcon: @Composable () -> Unit = {},
  actions: @Composable RowScope.() -> Unit = {},
  background: @Composable () -> Unit = {},
  windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
  colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
  content: @Composable CollapsibleTopAppBarScope.() -> Unit,
) = CollapsibleTopAppBar(
  modifier = modifier,
  windowInsets = windowInsets,
  colors = colors,
  scrollBehavior = scrollBehavior,
) {
  Box(
    modifier = Modifier
      .parallax()
      .fadeOnCollapse()
  ) {
    background()
  }

  Column {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      Box(
        modifier = Modifier
          .padding(start = TopAppBarHorizontalPadding)
          .pinned()
      ) {
        CompositionLocalProvider(
          LocalContentColor provides colors.navigationIconContentColor,
          content = navigationIcon
        )
      }

      Box(
        modifier = Modifier
          .padding(horizontal = TopAppBarHorizontalPadding)
          .weight(1f)
          .road(Alignment.TopStart, Alignment.BottomCenter)
      ) {
        val merged = LocalTextStyle.current.merge(MaterialTheme.typography.titleLarge)
        CompositionLocalProvider(
          LocalTextStyle provides merged,
          LocalContentColor provides colors.titleContentColor,
          content = title
        )
      }

      Box(
        modifier = Modifier
          .padding(end = TopAppBarHorizontalPadding)
          .pinned()
          .fadeOnCollapse()
      ) {
        CompositionLocalProvider(
          LocalContentColor provides colors.actionIconContentColor,
        ) {
          Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            content = actions
          )
        }
      }
    }

    Box(
      modifier = Modifier
        .padding(top = 8.dp)
    ) {
      content()
    }
  }
}

@Composable
fun CollapsibleTopAppBar(
  scrollBehavior: TopAppBarScrollBehavior,
  modifier: Modifier = Modifier,
  windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
  colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
  content: @Composable CollapsibleTopAppBarScope.() -> Unit,
) {
  val dragModifier = if (scrollBehavior.isPinned) Modifier else Modifier.draggable(
    orientation = Orientation.Vertical,
    state = rememberDraggableState { delta -> scrollBehavior.state.heightOffset += delta },
    onDragStopped = { velocity ->
      settleAppBar(
        scrollBehavior.state,
        velocity,
        scrollBehavior.flingAnimationSpec,
        scrollBehavior.snapAnimationSpec,
      )
    }
  )

  Surface(
    color = colors.containerColor,
    modifier = modifier.then(dragModifier)
  ) {
    CollapsibleTopAppBarLayout(
      scrollBehavior = scrollBehavior,
      content = content,
      modifier = Modifier
        .windowInsetsPadding(windowInsets)
        .clipToBounds(),
    )
  }
}

@Composable
private fun CollapsibleTopAppBarLayout(
  scrollBehavior: TopAppBarScrollBehavior,
  modifier: Modifier = Modifier,
  content: @Composable CollapsibleTopAppBarScope.() -> Unit,
) {
  val scope = CollapsibleTopAppBarScopeImpl(scrollBehavior.state)
  val measurePolicy = remember(scrollBehavior.state) { CollapsibleTopAppBarLayoutMeasurePolicy(scrollBehavior.state) }

  Layout(
    content = { scope.content() },
    modifier = modifier,
    measurePolicy = measurePolicy,
  )
}

private class CollapsibleTopAppBarLayoutMeasurePolicy(
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
private fun Placeable.placeCollapsible(maxWidth: Int, layoutHeight: Int, progress: Float, heightOffset: Float) = with(placementScope) {
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

  fun Modifier.fadeOnExpand(targetAlpha: Float = 0f): Modifier = fade(1f, targetAlpha)
  fun Modifier.fadeOnCollapse(targetAlpha: Float = 0f): Modifier = fade(targetAlpha, 1f)
  fun Modifier.fade(collapsed: Float, expanded: Float): Modifier

  fun Modifier.background(collapsedColor: Color, expandedColor: Color, collapsedShape: Shape = RectangleShape, expandedShape: Shape = collapsedShape): Modifier

  fun Modifier.pinned(): Modifier
  fun Modifier.parallax(ratio: Float = 0.2f): Modifier
  fun Modifier.road(collapsed: Alignment, expanded: Alignment): Modifier
}

context(collapsibleScope: CollapsibleTopAppBarScope, rowScope: RowScope)
fun Modifier.pinned(): Modifier {
  return this then layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)

    layout(placeable.width, placeable.height) {
      
    }
  }
}

private class CollapsibleTopAppBarScopeImpl(
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

  override fun Modifier.pinned() = this then PinnedElement

  override fun Modifier.parallax(ratio: Float) = this then ParallaxElement(ratio)

  override fun Modifier.road(collapsed: Alignment, expanded: Alignment) = this then RoadElement(collapsed, expanded)
}

val TopAppBarState.progress: Float
  get() = 1 - collapsedFraction

private data object PinnedElement : ModifierNodeElement<PinnedModifier>() {
  override fun create(): PinnedModifier {
    return PinnedModifier()
  }

  override fun update(node: PinnedModifier) {

  }
}

private class PinnedModifier : ParentDataModifierNode, CollapsibleTopAppBarPinnedData, Modifier.Node() {
  override fun Density.modifyParentData(parentData: Any?): Any? {
    return this@PinnedModifier
  }
}

private data class ParallaxElement(private val ratio: Float) : ModifierNodeElement<ParallaxModifier>() {
  override fun create(): ParallaxModifier {
    return ParallaxModifier(ratio)
  }

  override fun update(node: ParallaxModifier) {
    node.ratio = ratio
  }

  override fun InspectorInfo.inspectableProperties() {
    name = "parallax"
    value = ratio
  }
}

private class ParallaxModifier(ratio: Float) : ParentDataModifierNode, CollapsibleTopAppBarParallaxData, Modifier.Node() {
  override var ratio: Float = ratio
    internal set

  override fun Density.modifyParentData(parentData: Any?): Any? {
    return this@ParallaxModifier
  }
}

private data class RoadElement(
  private val collapsed: Alignment,
  private val expanded: Alignment,
) : ModifierNodeElement<RoadModifier>() {
  override fun create(): RoadModifier {
    return RoadModifier(collapsed, expanded)
  }

  override fun update(node: RoadModifier) {
    node.collapsed = collapsed
    node.expanded = expanded
  }

  override fun InspectorInfo.inspectableProperties() {
    name = "road"
    properties["collapsed"] = collapsed
    properties["expanded"] = expanded
  }
}

private class RoadModifier(
  collapsed: Alignment,
  expanded: Alignment,
) : ParentDataModifierNode, CollapsibleTopAppBarRoadData, Modifier.Node() {
  override var collapsed: Alignment = collapsed
    internal set
  override var expanded: Alignment = expanded
    internal set

  override fun Density.modifyParentData(parentData: Any?): Any? {
    return this@RoadModifier
  }
}

private sealed interface CollapsibleTopAppBarData
private interface CollapsibleTopAppBarPinnedData : CollapsibleTopAppBarData
private interface CollapsibleTopAppBarParallaxData : CollapsibleTopAppBarData {
  val ratio: Float
}
private interface CollapsibleTopAppBarRoadData : CollapsibleTopAppBarData {
  val collapsed: Alignment
  val expanded: Alignment
}

private data class RoadData(val collapsed: Alignment, val expanded: Alignment)

private val Placeable.isPinned: Boolean
  get() = parentData is CollapsibleTopAppBarPinnedData

private val Placeable.parallaxRatio: Float?
  get() = (parentData as? CollapsibleTopAppBarParallaxData)?.ratio

private val Placeable.roadData: RoadData?
  get() = (parentData as? CollapsibleTopAppBarRoadData)?.let {
    RoadData(it.collapsed, it.expanded)
  }

@OptIn(ExperimentalMaterial3Api::class)
private suspend fun settleAppBar(
  state: TopAppBarState,
  velocity: Float,
  flingAnimationSpec: DecayAnimationSpec<Float>?,
  snapAnimationSpec: AnimationSpec<Float>?,
): Velocity {
  // Check if the app bar is completely collapsed/expanded. If so, no need to settle the app bar,
  // and just return Zero Velocity.
  // Note that we don't check for 0f due to float precision with the collapsedFraction
  // calculation.
  if (state.collapsedFraction < 0.01f || state.collapsedFraction == 1f) {
    return Velocity.Zero
  }
  var remainingVelocity = velocity
  // In case there is an initial velocity that was left after a previous user fling, animate to4
  // continue the motion to expand or collapse the app bar.
  if (flingAnimationSpec != null && abs(velocity) > 1f) {
    var lastValue = 0f
    AnimationState(
      initialValue = 0f,
      initialVelocity = velocity,
    )
      .animateDecay(flingAnimationSpec) {
        val delta = value - lastValue
        val initialHeightOffset = state.heightOffset
        state.heightOffset = initialHeightOffset + delta
        val consumed = abs(initialHeightOffset - state.heightOffset)
        lastValue = value
        remainingVelocity = this.velocity
        // avoid rounding errors and stop if anything is unconsumed
        if (abs(delta - consumed) > 0.5f) this.cancelAnimation()
      }
  }
  // Snap if animation specs were provided.
  if (snapAnimationSpec != null) {
    if (state.heightOffset < 0 && state.heightOffset > state.heightOffsetLimit) {
      AnimationState(initialValue = state.heightOffset).animateTo(
        if (state.collapsedFraction < 0.5f) {
          0f
        } else {
          state.heightOffsetLimit
        },
        animationSpec = snapAnimationSpec
      ) {
        state.heightOffset = value
      }
    }
  }

  return Velocity(0f, remainingVelocity)
}

val TopAppBarHorizontalPadding = 4.dp