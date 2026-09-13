package com.kronos.skilletapp.model

import java.util.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class Equipment(
  val name: String,
  val id: String = Uuid.random().toString(),
)
