@file:OptIn(ExperimentalMaterial3Api::class)

package com.kronos.skilletapp.ui.component.collapsible

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layout
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ParentDataModifierNode
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Velocity
import kotlin.math.abs

context(_: CollapsibleTopAppBarScope)
fun Modifier.pinned() = this then PinnedElement

context(_: CollapsibleTopAppBarScope)
fun Modifier.parallax(ratio: Float = 0.2f) = this then ParallaxElement(ratio)

context(_: CollapsibleTopAppBarScope)
fun Modifier.road(collapsed: Alignment, expanded: Alignment) = this then RoadElement(collapsed, expanded)

context(_: CollapsibleTopAppBarScope)
fun Modifier.horizontalRoad(collapsed: Alignment.Horizontal, expanded: Alignment.Horizontal) = this then HorizontalRoadElement(collapsed, expanded)

context(_: CollapsibleTopAppBarScope)
fun Modifier.verticalRoad(collapsed: Alignment.Vertical, expanded: Alignment.Vertical) = this then VerticalRoadElement(collapsed, expanded)

context(collapsibleScope: CollapsibleTopAppBarScope, _: RowScope)
fun Modifier.road(collapsed: Alignment.Horizontal, expanded: Alignment.Horizontal) = this then Modifier.horizontalRoad(collapsed, expanded) then Modifier.collapsibleLayout()

context(collapsibleScope: CollapsibleTopAppBarScope, _: ColumnScope)
fun Modifier.road(collapsed: Alignment.Vertical, expanded: Alignment.Vertical) = this then Modifier.verticalRoad(collapsed, expanded) then Modifier.collapsibleLayout()

context(collapsibleScope: CollapsibleTopAppBarScope)
fun Modifier.collapsibleLayout(): Modifier = this then Modifier.layout { measurable, constraints ->
  val placeable = measurable.measure(constraints)

  layout(placeable.width, placeable.height) {
    placeable.placeCollapsible(placeable.width, placeable.height, collapsibleScope.progress, collapsibleScope.heightOffset)
  }
}

@Composable
fun Modifier.topAppBarDragBehavior(scrollBehavior: TopAppBarScrollBehavior): Modifier = this then if (scrollBehavior.isPinned) Modifier else Modifier.draggable(
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

private data object PinnedElement : ModifierNodeElement<PinnedModifier>() {
  override fun create(): PinnedModifier {
    return PinnedModifier()
  }

  override fun update(node: PinnedModifier) {

  }

  override fun InspectorInfo.inspectableProperties() {
    name = "pinned"
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

private data class HorizontalRoadElement(
  private val collapsed: Alignment.Horizontal,
  private val expanded: Alignment.Horizontal,
) : ModifierNodeElement<HorizontalRoadModifier>() {
  override fun create(): HorizontalRoadModifier {
    return HorizontalRoadModifier(collapsed, expanded)
  }

  override fun update(node: HorizontalRoadModifier) {
    node.collapsed = collapsed
    node.expanded = expanded
  }

  override fun InspectorInfo.inspectableProperties() {
    name = "horizontalRoad"
    properties["collapsed"] = collapsed
    properties["expanded"] = expanded
  }
}

private class HorizontalRoadModifier(
  collapsed: Alignment.Horizontal,
  expanded: Alignment.Horizontal,
) : ParentDataModifierNode, CollapsibleTopAppBarHorizontalRoadData, Modifier.Node() {
  override var collapsed: Alignment.Horizontal = collapsed
    internal set
  override var expanded: Alignment.Horizontal = expanded
    internal set

  override fun Density.modifyParentData(parentData: Any?): Any? {
    return this@HorizontalRoadModifier
  }
}

private data class VerticalRoadElement(
  private val collapsed: Alignment.Vertical,
  private val expanded: Alignment.Vertical,
) : ModifierNodeElement<VerticalRoadModifier>() {
  override fun create(): VerticalRoadModifier {
    return VerticalRoadModifier(collapsed, expanded)
  }

  override fun update(node: VerticalRoadModifier) {
    node.collapsed = collapsed
    node.expanded = expanded
  }

  override fun InspectorInfo.inspectableProperties() {
    name = "verticalRoad"
    properties["collapsed"] = collapsed
    properties["expanded"] = expanded
  }
}

private class VerticalRoadModifier(
  collapsed: Alignment.Vertical,
  expanded: Alignment.Vertical,
) : ParentDataModifierNode, CollapsibleTopAppBarVerticalRoadData, Modifier.Node() {
  override var collapsed: Alignment.Vertical = collapsed
    internal set
  override var expanded: Alignment.Vertical = expanded
    internal set

  override fun Density.modifyParentData(parentData: Any?): Any? {
    return this@VerticalRoadModifier
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

private interface CollapsibleTopAppBarHorizontalRoadData : CollapsibleTopAppBarData {
  val collapsed: Alignment.Horizontal
  val expanded: Alignment.Horizontal
}

private interface CollapsibleTopAppBarVerticalRoadData : CollapsibleTopAppBarData {
  val collapsed: Alignment.Vertical
  val expanded: Alignment.Vertical
}

data class RoadData(val collapsed: Alignment, val expanded: Alignment)
data class HorizontalRoadData(val collapsed: Alignment.Horizontal, val expanded: Alignment.Horizontal)
data class VerticalRoadData(val collapsed: Alignment.Vertical, val expanded: Alignment.Vertical)

private val Placeable.isPinned: Boolean
  get() = parentData is CollapsibleTopAppBarPinnedData

val Placeable.parallaxRatio: Float?
  get() = (parentData as? CollapsibleTopAppBarParallaxData)?.ratio

val Placeable.roadData: RoadData?
  get() = (parentData as? CollapsibleTopAppBarRoadData)?.let {
    RoadData(it.collapsed, it.expanded)
  }

val Placeable.horizontalRoadData: HorizontalRoadData?
  get() = (parentData as? CollapsibleTopAppBarHorizontalRoadData)?.let {
    HorizontalRoadData(it.collapsed, it.expanded)
  }

val Placeable.verticalRoadData: VerticalRoadData?
  get() = (parentData as? CollapsibleTopAppBarVerticalRoadData)?.let {
    VerticalRoadData(it.collapsed, it.expanded)
  }

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