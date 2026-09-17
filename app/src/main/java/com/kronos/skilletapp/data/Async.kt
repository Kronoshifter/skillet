package com.kronos.skilletapp.data

import com.kronos.skilletapp.model.SkilletError

sealed interface Async<out T> {
  val data: T? get() = null
  val error: SkilletError? get() = null
  val isLoading: Boolean get() = false

  data object Idle : Async<Nothing>
  data object Loading : Async<Nothing> {
    override val isLoading: Boolean get() = true
  }

  data class Success<out T>(override val data: T) : Async<T>
  data class Failure(override val error: SkilletError) : Async<Nothing>

  companion object {
    fun <T> idle(): Async<T> = Idle
    fun <T> loading(): Async<T> = Loading
    fun <T> success(data: T): Async<T> = Success(data)
    fun <T> failure(error: SkilletError): Async<T> = Failure(error)
  }
}
