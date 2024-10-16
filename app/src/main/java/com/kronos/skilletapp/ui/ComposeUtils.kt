package com.kronos.skilletapp.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kronos.skilletapp.data.SkilletError
import com.kronos.skilletapp.data.UiState

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> LoadingContent(
  state: UiState<T>,
  modifier: Modifier = Modifier,
  loadingContent: @Composable () -> Unit = { CircularProgressIndicator() },
  errorContent: @Composable (SkilletError) -> Unit = { error -> Text(text = error.message) },
  content: @Composable (data: T) -> Unit,
) {
  AnimatedContent(
    targetState = state,
    label = "Loading",
    modifier = Modifier.fillMaxSize(),
  ) { targetState ->
    Box(contentAlignment = Alignment.Center, modifier = Modifier
      .fillMaxSize()
      .then(modifier)) {
      when (targetState) {
        UiState.Loading -> loadingContent()
        is UiState.Error -> errorContent(targetState.error)
        is UiState.LoadedWithData -> content(targetState.data)
        UiState.Loaded -> throw IllegalStateException("Invalid state: $targetState")
      }
    }
  }
}

@Composable
fun LoadingContent(
  state: UiState<Nothing>,
  modifier: Modifier = Modifier,
  loadingContent: @Composable () -> Unit = { CircularProgressIndicator() },
  errorContent: @Composable (SkilletError) -> Unit = { error -> Text(text = error.message) },
  content: @Composable () -> Unit,
  ) {
  AnimatedContent(
    targetState = state,
    label = "Loading",
    modifier = Modifier.fillMaxSize(),
  ) { targetState ->
    Box(contentAlignment = Alignment.Center, modifier = Modifier
      .fillMaxSize()
      .then(modifier)) {
      when (targetState) {
        UiState.Loading -> loadingContent()
        is UiState.Error -> errorContent(targetState.error)
        UiState.Loaded -> content()
        is UiState.LoadedWithData -> throw IllegalStateException("Invalid state: $targetState")
      }
    }
  }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RefreshingContent(
  refreshing: Boolean,
  onRefresh: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  val refreshState = rememberPullRefreshState(refreshing = refreshing, onRefresh = onRefresh)

  Box(
    modifier = Modifier
      .pullRefresh(refreshState)
      .then(modifier)
  ) {
    content()
    PullRefreshIndicator(refreshing = refreshing, state = refreshState, modifier = Modifier.align(Alignment.TopCenter))
  }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisableRipple(
  content: @Composable () -> Unit
) = CompositionLocalProvider(
  value = LocalRippleConfiguration provides null,
  content = content
)