package com.kronos.skilletapp.utils

fun <T> List<T>.move(from: Int, to: Int): List<T> = toMutableList().apply {
  add(to, removeAt(from))
}