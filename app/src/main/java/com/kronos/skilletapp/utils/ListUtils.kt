package com.kronos.skilletapp.utils

fun <T, R : Comparable<R>> List<T>.update(
  item: T,
  selector: (T) -> R,
) = map { if (selector(it) == selector(item)) item else it }

fun <T, R : Comparable<R>> List<T>.upsert(
  item: T,
  selector: (T) -> R,
) = if (any { selector(it) == selector(item) }) {
  map { if (selector(it) == selector(item)) item else it }
} else {
  this + item
}

fun <T> List<T>.move(from: Int, to: Int): List<T> = toMutableList().apply { add(to, removeAt(from)) }.toList()