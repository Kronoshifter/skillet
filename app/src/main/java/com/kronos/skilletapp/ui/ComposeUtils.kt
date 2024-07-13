package com.kronos.skilletapp.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LoadingContent(
  loading: Boolean,
  empty: Boolean,
  emptyContent: @Composable () -> Unit,
  onRefresh: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  if (empty) {
    emptyContent()
  } else {
    val state = rememberPullRefreshState(refreshing = loading, onRefresh = onRefresh)
    
    Box(
      modifier = Modifier.pullRefresh(state).then(modifier),
    ) {
      content()
      PullRefreshIndicator(refreshing = loading, state = state, modifier = Modifier.align(Alignment.TopCenter))
    }
  }
}