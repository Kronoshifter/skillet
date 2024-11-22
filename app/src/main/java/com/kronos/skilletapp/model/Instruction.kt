package com.kronos.skilletapp.model

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Instruction(
  val text: String,
  val image: String? = null,
  val equipment: List<Equipment> = emptyList(),
  val ingredients: List<Ingredient> = emptyList(),
  val id: String = UUID.randomUUID().toString(),
)
