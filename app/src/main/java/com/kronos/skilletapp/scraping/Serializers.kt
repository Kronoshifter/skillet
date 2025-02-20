package com.kronos.skilletapp.scraping

import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.serializer

object StringListUnwrappingSerializer : JsonTransformingSerializer<String>(String.serializer()) {
  override fun transformDeserialize(element: JsonElement): JsonElement {
    if (element !is JsonArray) return element
    require(element.size == 1) { "Expected a single element, but got ${element.size}" }
    return element.first()
  }
}

object StringListWrappingSerializer : JsonTransformingSerializer<List<String>>(serializer<List<String>>()) {
  override fun transformDeserialize(element: JsonElement): JsonElement = element as? JsonArray ?: buildJsonArray { add(element) }
}

inline fun <reified T: Any> listUnwrappingSerializer(): JsonTransformingSerializer<T> {
  return object : JsonTransformingSerializer<T>(serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement = if (element is JsonArray) element.first() else element
  }
}