package com.kronos.skilletapp.scraping

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement

class ListOrStringSerializer : JsonContentPolymorphicSerializer<String>(String::class) {
  override fun selectDeserializer(element: JsonElement): DeserializationStrategy<String> = if (element is JsonArray) {
    ListAsStringSerializer
  } else {
    String.serializer()
  }

  private object ListAsStringSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor = String.serializer().descriptor

    override fun deserialize(decoder: Decoder): String {
      return ListSerializer(String.serializer()).deserialize(decoder).first()
    }

    override fun serialize(encoder: Encoder, value: String) {
      throw UnsupportedOperationException("This serializer is only used for deserialization")
    }
  }
}