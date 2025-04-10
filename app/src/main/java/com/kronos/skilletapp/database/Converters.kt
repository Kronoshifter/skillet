package com.kronos.skilletapp.database

import android.util.Log
import androidx.room.TypeConverter
import com.github.michaelbull.result.getOrElse
import com.kronos.skilletapp.model.Equipment
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.Instruction
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.github.michaelbull.result.runCatching

class RecipeConverters {
  private val json = Json { ignoreUnknownKeys = true }
  
  @TypeConverter
  fun ingredientsFromJson(value: String): List<Ingredient> {
    return runCatching {
      json.decodeFromString<List<Ingredient>>(value)
    }.getOrElse {
      Log.d("ConvertersJson", value)
      Log.e("Converters", "Failed to convert ingredients: ${it.message}", it)
      emptyList()
    }
  }

  @TypeConverter
  fun ingredientsToJson(value: List<Ingredient>): String = json.encodeToString(value)

  @TypeConverter
  fun instructionsFromJson(value: String): List<Instruction> {
    return runCatching {
      json.decodeFromString<List<Instruction>>(value)
    }.getOrElse {
      Log.d("ConvertersJson", value)
      Log.e("Converters", "Failed to convert instructions: ${it.message}", it)
      emptyList()
    }
  }

  @TypeConverter
  fun instructionsToJson(value: List<Instruction>): String = json.encodeToString(value)

  @TypeConverter
  fun equipmentFromJson(value: String): List<Equipment> {
    return runCatching {
      json.decodeFromString<List<Equipment>>(value)
    }.getOrElse {
      Log.d("ConvertersJson", value)
      Log.e("Converters", "Failed to convert equipment: ${it.message}", it)
      emptyList()
    }
  }

  @TypeConverter
  fun equipmentToJson(value: List<Equipment>): String = json.encodeToString(value)
}