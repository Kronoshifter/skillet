package com.kronos.skilletapp.utils

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
import com.github.michaelbull.result.onErr
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun <T> T.ok() = Ok(this)
fun <T> T.err() = Err(this)

@OptIn(ExperimentalContracts::class, UnsafeResultValueAccess::class)
inline infix fun <V, E> Result<V, E>.guard(onGuard: (E) -> Unit): V {
  contract {
    callsInPlace(onGuard, InvocationKind.AT_MOST_ONCE)
  }

  onErr(onGuard)
  require(isOk) { "guard can only return Ok, ensure that onGuard stops execution" }

  return value
}