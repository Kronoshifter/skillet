package com.kronos.skilletapp.model

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Ingredient(
  val name: String,
  val measurement: Measurement,
  val raw: String,
  val comment: String? = null,
  val id: String = UUID.randomUUID().toString(),
) {
  override fun toString(): String {
    return "$measurement $name${comment?.let { ", $it" } ?: ""}"
  }
}

enum class IngredientType {
  Wet,
  Dry
}