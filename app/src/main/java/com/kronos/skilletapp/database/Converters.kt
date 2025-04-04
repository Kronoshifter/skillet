package com.kronos.skilletapp.database

import androidx.room.TypeConverter
import com.kronos.skilletapp.model.Equipment
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.Instruction
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RecipeConverters {
  private val json = Json { ignoreUnknownKeys = true }
  
  @TypeConverter
  fun ingredientsFromJson(value: String): List<Ingredient> = json.decodeFromString(value)

  @TypeConverter
  fun ingredientsToJson(value: List<Ingredient>): String = json.encodeToString(value)

  @TypeConverter
  fun instructionsFromJson(value: String): List<Instruction> = json.decodeFromString(value)

  @TypeConverter
  fun instructionsToJson(value: List<Instruction>): String = json.encodeToString(value)

  @TypeConverter
  fun equipmentFromJson(value: String): List<Equipment> = json.decodeFromString(value)

  @TypeConverter
  fun equipmentToJson(value: List<Equipment>): String = json.encodeToString(value)
}