package com.kronos.skilletapp.model

import kotlinx.serialization.Serializable
import java.util.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class Equipment(
  val name: String,
  val id: String = Uuid.random().toString()
)