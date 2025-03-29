package com.kronos.skilletapp.model

import kotlinx.serialization.Serializable
import java.util.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class Ingredient(
  val name: String,
  val measurement: Measurement<*, *>,
  val raw: String,
  val comment: String? = null,
  val id: String = Uuid.random().toString(),
) {
  override fun toString(): String {
    return "$measurement $name${comment?.let { ", $it" } ?: ""}"
  }
}

enum class IngredientType {
  Wet,
  Dry
}