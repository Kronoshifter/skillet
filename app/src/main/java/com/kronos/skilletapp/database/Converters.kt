package com.kronos.skilletapp.database

import androidx.room.TypeConverter
import com.kronos.skilletapp.model.Equipment
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.Instruction
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RecipeConverters {
  @TypeConverter
  fun ingredientsFromJson(value: String): List<Ingredient> = Json.decodeFromString(value)

  @TypeConverter
  fun ingredientsToJson(value: List<Ingredient>): String = Json.encodeToString(value)

  @TypeConverter
  fun instructionsFromJson(value: String): List<Instruction> = Json.decodeFromString(value)

  @TypeConverter
  fun instructionsToJson(value: List<Instruction>): String = Json.encodeToString(value)

  @TypeConverter
  fun equipmentFromJson(value: String): List<Equipment> = Json.decodeFromString(value)

  @TypeConverter
  fun equipmentToJson(value: List<Equipment>): String = Json.encodeToString(value)
}