package com.kronos.skilletapp.parser.python

import android.content.Context
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.Measurement
import com.kronos.skilletapp.model.MeasurementUnit
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class IngredientSlice(
  val ingredient: String,
  val standardized_ingredient: String,
  val food: String,
  val quantity: Float?,
  val unit: String?,
  val standardized_unit: String?,
  val secondary_quantity: Float?,
  val secondary_unit: String?,
  val standardized_secondary_unit: String?,
  val density: Float?,
  val gram_weight: Float?,
  val prep: List<String>,
  val size_modifiers: List<String>,
  val dimensions: List<String>,
  val is_required: Boolean,
  val parenthesis_content: List<String>
) {
  fun toIngredient() = Ingredient(
    name = food,
    raw = ingredient,
    measurement = Measurement(
      quantity = quantity ?: 0f,
      unit = MeasurementUnit.fromName(standardized_unit)
    ),
    comment = (size_modifiers + prep).joinToString(", ").let { it.ifEmpty { null } }
  )
}

class IngredientSlicer(private val context: Context) {

  fun parse(text: String): Ingredient {
    if (!Python.isStarted()) {
      Python.start(AndroidPlatform(context))
    }

    val python = Python.getInstance()
    val module = python.getModule("parse")

    val json = parseToJson(text, module)

    val result = decodeJson(json).toIngredient()

    return result
  }

  fun parseList(text: String): List<Ingredient> {
    if (!Python.isStarted()) {
      Python.start(AndroidPlatform(context))
    }

    val python = Python.getInstance()
    val module = python.getModule("parse")

    val result = text.split("\n").map {
      val json = parseToJson(it, module)
      decodeJson(json).toIngredient()
    }

    return result
  }

  private fun parseToJson(text: String, module: PyObject) = module.callAttr("parse", text).toJava(String::class.java)
  private fun decodeJson(json: String) = Json.decodeFromString<IngredientSlice>(json)
}