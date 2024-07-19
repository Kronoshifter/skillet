package com.kronos.skilletapp.model

data class Ingredient(
  val name: String,
  val type: IngredientType,
  val comment: String? = null,
  val measurement: Measurement,
)

enum class IngredientType {
  Wet,
  Dry
}