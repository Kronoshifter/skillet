package com.kronos.skilletapp.database

import androidx.room.TypeConverter
import com.kronos.skilletapp.model.Ingredient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
  @TypeConverter
  fun ingredientsFromJson(value: String): List<Ingredient> = Json.decodeFromString(value)

  @TypeConverter
  fun ingredientsToJson(value: List<Ingredient>): String = Json.encodeToString(value)
}