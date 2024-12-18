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
import kotlinx.serialization.serializer

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

inline fun <reified T : Any> listOrSingleSerializer(): JsonContentPolymorphicSerializer<T> {
  return object : JsonContentPolymorphicSerializer<T>(T::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<T> = if (element is JsonArray) {
      object : KSerializer<T> {
        override val descriptor: SerialDescriptor = serializer<T>().descriptor

        override fun deserialize(decoder: Decoder): T {
          return serializer<List<T>>().deserialize(decoder).first()
        }

        override fun serialize(encoder: Encoder, value: T) {
          throw UnsupportedOperationException("This serializer is only used for deserialization")
        }
      }
    } else {
      serializer<T>()
    }


  }
}