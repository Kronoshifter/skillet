package com.kronos.skilletapp.utils.modifier

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer

private const val FADE_EDGE_START_STOP = 0f
private const val FADE_EDGE_MID_STOP = 0.5f
private const val FADE_EDGE_END_STOP = 1f

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
            FADE_EDGE_START_STOP to Color.Transparent,
            FADE_EDGE_MID_STOP to Color.Black,
            FADE_EDGE_END_STOP to Color.Transparent,
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
            FADE_EDGE_START_STOP to Color.Transparent,
            FADE_EDGE_MID_STOP to Color.Black,
            FADE_EDGE_END_STOP to Color.Transparent,
          ),
        blendMode = BlendMode.DstIn,
      )
    }
