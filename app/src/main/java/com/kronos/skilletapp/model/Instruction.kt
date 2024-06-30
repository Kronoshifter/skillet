package com.kronos.skilletapp.model

data class Instruction(
  val text: String,
  val image: String? = null,
  val equipment: List<Equipment> = emptyList(),
  val ingredients: List<Ingredient> = emptyList(),
)
