@file:OptIn(ExperimentalMaterial3Api::class)

package com.kronos.skilletapp.ui.component.collapsible

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.*
import androidx.compose.ui.unit.*
import kotlin.math.abs
//
//@Composable
//fun CollapsibleTopAppBar(
//  title: @Composable context(CollapsibleTopAppBarScope) () -> Unit,
//  scrollBehavior: TopAppBarScrollBehavior,
//  modifier: Modifier = Modifier,
//  navigationIcon: @Composable () -> Unit = {},
//  actions: @Composable RowScope.() -> Unit = {},
//  background: @Composable () -> Unit = {},
//  windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
//  colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
//  content: @Composable CollapsibleTopAppBarScope.() -> Unit,
//) = CollapsibleTopAppBar(
//  modifier = modifier,
//  windowInsets = windowInsets,
//  colors = colors,
//  scrollBehavior = scrollBehavior,
//) {
//  Box(
//    modifier = Modifier
//      .parallax()
//      .fadeOnCollapse()
//  ) {
//    background()
//  }
//
//  Column(
//    modifier = Modifier.wrapContentHeight()
//  ) {
//    Row(
//      verticalAlignment = Alignment.CenterVertically,
//      modifier = Modifier.fillMaxWidth()
//    ) {
//      Box(
//        modifier = Modifier
//          .padding(start = TopAppBarHorizontalPadding)
//          .pinned()
//      ) {
//        CompositionLocalProvider(
//          LocalContentColor provides colors.navigationIconContentColor,
//          content = navigationIcon
//        )
//      }
//
//      Box(
//        modifier = Modifier
//          .padding(horizontal = TopAppBarHorizontalPadding)
//          .wrapContentHeight()
//          .weight(1f)
//      ) {
//        val merged = LocalTextStyle.current.merge(MaterialTheme.typography.titleLarge)
//        CompositionLocalProvider(
//          LocalTextStyle provides merged,
//          LocalContentColor provides colors.titleContentColor,
//        ) {
//          title()
//        }
//      }
//
//      Box(
//        modifier = Modifier
//          .padding(end = TopAppBarHorizontalPadding)
//          .pinned()
//          .fadeOnCollapse()
//      ) {
//        CompositionLocalProvider(
//          LocalContentColor provides colors.actionIconContentColor,
//        ) {
//          Row(
//            horizontalArrangement = Arrangement.End,
//            verticalAlignment = Alignment.CenterVertically,
//            content = actions
//          )
//        }
//      }
//    }
//
//    Box(
//      modifier = Modifier
//        .padding(top = 8.dp)
//    ) {
//      content()
//    }
//  }
//}
//
//@Composable
//fun CollapsibleTopAppBar(
//  scrollBehavior: TopAppBarScrollBehavior,
//  modifier: Modifier = Modifier,
//  windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
//  colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
//  content: @Composable CollapsibleTopAppBarScope.() -> Unit,
//) {
//  val dragModifier = Modifier.topAppBarDragBehavior(scrollBehavior)
//
//  Surface(
//    color = colors.containerColor,
//    modifier = modifier.then(dragModifier)
//  ) {
//    CollapsibleTopAppBarLayout(
//      scrollBehavior = scrollBehavior,
//      content = content,
//      modifier = Modifier
//        .windowInsetsPadding(windowInsets)
//        .clipToBounds(),
//    )
//  }
//}
//
//@Composable
//private fun CollapsibleTopAppBarLayout(
//  scrollBehavior: TopAppBarScrollBehavior,
//  modifier: Modifier = Modifier,
//  content: @Composable CollapsibleTopAppBarScope.() -> Unit,
//) {
//  val scope = CollapsibleTopAppBarScopeImpl(scrollBehavior.state)
//  val measurePolicy = remember(scrollBehavior.state) { CollapsibleTopAppBarLayoutMeasurePolicy(scrollBehavior.state) }
//
//  Layout(
//    content = { scope.content() },
//    modifier = modifier,
//    measurePolicy = measurePolicy,
//  )
//}
//
//val TopAppBarHorizontalPadding = 4.dp