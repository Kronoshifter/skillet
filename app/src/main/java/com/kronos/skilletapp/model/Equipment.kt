package com.kronos.skilletapp.model

import java.util.*

data class Equipment(
  val name: String,
  val id: String = UUID.randomUUID().toString()
)