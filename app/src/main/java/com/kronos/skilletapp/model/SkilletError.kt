package com.kronos.skilletapp.model

sealed class SkilletError(val message: String)

class InvalidFormError(message: String) : SkilletError(message)

class RecipeCouldNotBeLoadedError(message: String) : SkilletError(message)

class UnknownError(message: String) : SkilletError(message)

@Suppress("SpellCheckingInspection")
object UsedLoadedWhereYouShouldntError : SkilletError("UiState.Loaded should not be used here")

@Suppress("SpellCheckingInspection")
object UsedLoadedWithDataWhereYouShouldntError :
  SkilletError("UiState.LoadedWithData should not be used here")

sealed class RecipeScrapeError(message: String) : SkilletError(message)

class InvalidUrlError(message: String) : RecipeScrapeError(message)

class InvalidHtmlError(message: String) : RecipeScrapeError(message)

class JsonParseError(message: String) : RecipeScrapeError(message)
