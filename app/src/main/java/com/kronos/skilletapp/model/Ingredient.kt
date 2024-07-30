package com.kronos.skilletapp.model

import java.util.*

data class Ingredient(
  val name: String,
  val measurement: Measurement,
  val comment: String? = null,
  val id: String = UUID.randomUUID().toString(),
)

enum class IngredientType {
  Wet,
  Dry
}