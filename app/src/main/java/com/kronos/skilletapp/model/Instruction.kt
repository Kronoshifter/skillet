package com.kronos.skilletapp.model

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class Instruction(
  val text: String,
  val image: String? = null,
  val equipment: List<Equipment> = emptyList(),
  val ingredients: List<Ingredient> = emptyList(),
  val id: String = Uuid.random().toString(),
)
