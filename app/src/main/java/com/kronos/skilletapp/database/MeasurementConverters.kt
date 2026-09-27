package com.kronos.skilletapp.database

import androidx.room3.ColumnTypeConverter
import com.kronos.measurement.model.Measurement
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room [ColumnTypeConverter] for the single converted type, [Measurement], stored as one JSON column.
 *
 * Uses `Json { ignoreUnknownKeys = true }`. Because [MeasurementUnit] is a sealed `@Serializable` hierarchy annotated with
 * `@JsonClassDiscriminator("measurement_type")`, the round-trip preserves the discriminator for both named units (e.g. `Gram`, `Cup`,
 * `Tablespoon`) and `MeasurementUnit.Custom(name)`.
 */
class MeasurementConverters {
  private val json = Json { ignoreUnknownKeys = true }

  @ColumnTypeConverter fun measurementToJson(value: Measurement): String = json.encodeToString(value)

  @ColumnTypeConverter fun measurementFromString(value: String): Measurement = json.decodeFromString(value)
}
