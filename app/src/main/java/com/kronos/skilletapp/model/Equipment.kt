package com.kronos.skilletapp.model

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Equipment(
  val name: String,
  val id: String = UUID.randomUUID().toString()
)