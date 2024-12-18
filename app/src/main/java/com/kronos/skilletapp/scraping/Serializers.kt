package com.kronos.skilletapp.scraping

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.serializer

object StringListUnwrappingSerializer : JsonTransformingSerializer<String>(serializer<String>()) {
  override fun transformDeserialize(element: JsonElement): JsonElement = if (element is JsonArray) element.first() else element
}

inline fun <reified T: Any> listUnwrappingSerializer(): JsonTransformingSerializer<T> {
  return object : JsonTransformingSerializer<T>(serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement = if (element is JsonArray) element.first() else element
  }
}