package com.kronos.skilletapp.utils

import androidx.compose.ui.Modifier

inline fun Modifier.applyIf(condition: Boolean, block: Modifier.() -> Modifier) = if (condition) {
  this then block()
} else {
  this
}

inline fun Modifier.applyUnless(condition: Boolean, block: Modifier.() -> Modifier) = if (condition) {
  this
} else {
  this then block()
}