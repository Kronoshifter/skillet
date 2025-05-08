package com.kronos.skilletapp.utils.modifier

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.*

fun Modifier.applyIf(condition: Boolean, block: Modifier.() -> Modifier) = if (condition) {
  this then Modifier.block()
} else {
  this
}

fun Modifier.applyUnless(condition: Boolean, block: Modifier.() -> Modifier) = if (condition) {
  this
} else {
  this then Modifier.block()
}

fun <T> Modifier.applyIfNotNull(value: T?, block: Modifier.(T) -> Modifier) = if (value != null) {
  this then Modifier.block(value)
} else {
  this
}

@Composable
fun Modifier.applyIfComposable(condition: Boolean, block: @Composable Modifier.() -> Modifier) = if (condition) {
  this then Modifier.block()
} else {
  this
}

fun Modifier.verticalFadingEdge() = this then Modifier
  .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
  .drawWithContent {
    drawContent()
    drawRect(
      brush = Brush.verticalGradient(
        0f to Color.Transparent,
        0.5f to Color.Black,
        1f to Color.Transparent
      ),
      blendMode = BlendMode.DstIn
    )
  }

fun Modifier.horizontalFadingEdge() = this then Modifier
  .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
  .drawWithContent {
    drawContent()
    drawRect(
      brush = Brush.horizontalGradient(
        0f to Color.Transparent,
        0.5f to Color.Black,
        1f to Color.Transparent
      ),
      blendMode = BlendMode.DstIn
    )
  }