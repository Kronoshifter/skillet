package com.kronos.skilletapp.database

import androidx.room.TypeConverter
import com.kronos.measurement.model.Measurement
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room [TypeConverter] for the single converted type, [Measurement], stored as one JSON column.
 *
 * Uses `Json { ignoreUnknownKeys = true }`. Because
 * [MeasurementUnit] is a sealed `@Serializable` hierarchy annotated with
 * `@JsonClassDiscriminator("measurement_type")`, the round-trip preserves the discriminator for both
 * named units (e.g. `Gram`, `Cup`, `Tablespoon`) and `MeasurementUnit.Custom(name)`.
 *
 * Not yet registered in `@Database` (that is a later gate); it compiles as an orphan converter class
 * in this additive gate.
 */
class MeasurementConverters {
  private val json = Json { ignoreUnknownKeys = true }

  @TypeConverter fun measurementToJson(value: Measurement): String = json.encodeToString(value)

  @TypeConverter fun measurementFromString(value: String): Measurement = json.decodeFromString(value)
}
