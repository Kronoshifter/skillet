package com.kronos.skilletapp.utils.modifier

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.*

fun Modifier.applyIf(condition: Boolean, block: Modifier.() -> Modifier) =
  if (condition) {
    this then block()
  } else {
    this
  }

fun Modifier.applyUnless(condition: Boolean, block: Modifier.() -> Modifier) =
  if (condition) {
    this
  } else {
    this then block()
  }

fun <T> Modifier.applyIfNotNull(value: T?, block: Modifier.(T) -> Modifier) =
  if (value != null) {
    this then block(value)
  } else {
    this
  }

fun Modifier.verticalFadingEdge() =
  this then
    Modifier.graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen).drawWithContent {
      drawContent()
      drawRect(
        brush =
          Brush.verticalGradient(
            0f to Color.Transparent,
            0.5f to Color.Black,
            1f to Color.Transparent,
          ),
        blendMode = BlendMode.DstIn,
      )
    }

fun Modifier.horizontalFadingEdge() =
  this then
    Modifier.graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen).drawWithContent {
      drawContent()
      drawRect(
        brush =
          Brush.horizontalGradient(
            0f to Color.Transparent,
            0.5f to Color.Black,
            1f to Color.Transparent,
          ),
        blendMode = BlendMode.DstIn,
      )
    }
