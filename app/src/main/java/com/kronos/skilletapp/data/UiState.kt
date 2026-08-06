package com.kronos.skilletapp.data

import com.kronos.skilletapp.model.SkilletError

sealed interface UiState<out T> {
  data object Loading : UiState<Nothing>
  data object Loaded : UiState<Nothing>
  data class LoadedWithData<T>(val data: T) : UiState<T>
  data class Error(val error: SkilletError) : UiState<Nothing>
}