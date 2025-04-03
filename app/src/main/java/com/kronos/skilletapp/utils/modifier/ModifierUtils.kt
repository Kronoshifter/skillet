package com.kronos.skilletapp.utils.modifier

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.*

@Composable
fun Modifier.applyIf(condition: Boolean, block: @Composable Modifier.() -> Modifier) = if (condition) {
  this then block()
} else {
  this
}

@Composable
fun Modifier.applyUnless(condition: Boolean, block: @Composable Modifier.() -> Modifier) = if (condition) {
  this
} else {
  this then block()
}

@Composable
fun <T> Modifier.applyIfNotNull(value: T?, block: @Composable Modifier.(T) -> Modifier) = if (value != null) {
  this then block(value)
} else {
  this
}



fun Modifier.verticalFadingEdge() = this
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


fun Modifier.horizontalFadingEdge() = this
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