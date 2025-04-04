package com.kronos.skilletapp.utils

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass


inline fun <reified T> T.toJson(): String = Json.encodeToString(this)

inline fun <reified T> String.fromJson(): T = Json.decodeFromString(this)

inline fun <T> T.applyIf(condition: Boolean, block: T.() -> Unit): T {
  if (condition) {
    block()
  }

  return this
}

inline fun <T> T.applyUnless(condition: Boolean, block: T.() -> Unit): T = applyIf(!condition, block)

inline fun <T> T.mutateIf(condition: Boolean, block: T.() -> T): T = if (condition) {
  block()
} else {
  this
}

inline fun <T> T.mutateUnless(condition: Boolean, block: T.() -> T): T = mutateIf(!condition, block)

fun <A, B> Pair<A, B>.haveSameTypes(vararg klasses: KClass<*>): Boolean = klasses.any { it.isInstance(first) && it.isInstance(second) }
infix fun <A, B> Pair<A, B>.haveSameType(klass: KClass<*>): Boolean = haveSameTypes(klass)