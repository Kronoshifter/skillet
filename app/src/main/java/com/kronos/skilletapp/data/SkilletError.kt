package com.kronos.skilletapp.data

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

sealed class SkilletError(val message: String)

class InvalidFormError(message: String) : SkilletError(message)
class RecipeCouldNotBeLoadedError(message: String) : SkilletError(message)
class UnknownError(message: String) : SkilletError(message)
@Suppress("SpellCheckingInspection")
object UsedLoadedWhereYouShouldntError : SkilletError("UiState.Loaded should not be used here")
@Suppress("SpellCheckingInspection")
object UsedLoadedWithDataWhereYouShouldntError : SkilletError("UiState.LoadedWithData should not be used here")

sealed class RecipeScrapeError(message: String) : SkilletError(message)

class InvalidUrlError(message: String) : RecipeScrapeError(message)
class InvalidHtmlError(message: String) : RecipeScrapeError(message)
class JsonParseError(message: String) : RecipeScrapeError(message)

fun <T> T.ok() = Ok(this)
fun <T> T.err() = Err(this)

@OptIn(ExperimentalContracts::class)
inline infix fun <V, E> Result<V, E>.guard(onGuard: (E) -> Unit): V {
  contract {
    callsInPlace(onGuard, InvocationKind.AT_MOST_ONCE)
  }

  onFailure(onGuard)
  require(isOk) { "guard can only return Ok, ensure that onGuard stops execution" }

  return value
}