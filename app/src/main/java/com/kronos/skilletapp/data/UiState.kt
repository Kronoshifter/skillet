package com.kronos.skilletapp.data

sealed interface UiState<out T> {
  data object Loading : UiState<Nothing>
  data class Loaded<T>(val data: T) : UiState<T>
  data class Error(val error: SkilletError) : UiState<Nothing>
}