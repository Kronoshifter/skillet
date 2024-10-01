package com.kronos.skilletapp.ui.screen.cooking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.MeasurementUnit
import com.kronos.skilletapp.model.Recipe
import com.kronos.skilletapp.ui.LoadingContent
import com.kronos.skilletapp.ui.viewmodel.CookingViewModel
import org.koin.androidx.compose.getViewModel

private sealed class CookingContentTab {
  data object Overview: CookingContentTab()
  data class Instruction(val index: Int): CookingContentTab()
  data object Complete: CookingContentTab()

  fun index(recipe: Recipe): Int = when (this) {
    Overview -> 0
    is Instruction -> index + 1
    Complete -> recipe.instructions.size + 1
  }

  companion object {
    fun fromIndex(index: Int, recipe: Recipe): CookingContentTab = when (index) {
      0 -> Overview
      in 1..recipe.instructions.size -> Instruction(index - 1)
      else -> Complete
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingScreen(
  onBack: () -> Unit,
  vm: CookingViewModel = getViewModel(),
) {
  val uiState by vm.uiState.collectAsStateWithLifecycle()
  val selectedUnits by vm.selectedUnits.collectAsStateWithLifecycle()

  LoadingContent(
    state = uiState,
    modifier = Modifier
      .fillMaxSize()
  ) { recipe ->
    Scaffold(
      topBar = {
        TopAppBar(
          title = { Text(text = recipe.name) },
          navigationIcon = {
            IconButton(onClick = onBack) {
              Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
          }
        )
      }
    ) { paddingValues ->
      CookingContent(
        recipe = recipe,
        selectedUnits = selectedUnits,
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues),
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingContent(
  recipe: Recipe,
  selectedUnits: Map<Ingredient, MeasurementUnit?>,
  modifier: Modifier = Modifier
) {
  var tab: CookingContentTab by remember { mutableStateOf(CookingContentTab.Overview) }
  val pagerState = rememberPagerState { recipe.instructions.size + 2 }

  Surface(
    modifier = modifier,
    color = MaterialTheme.colorScheme.background
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      PrimaryScrollableTabRow(
        selectedTabIndex = tab.index(recipe),
        modifier = Modifier.fillMaxWidth()
      ) {
        Tab(
          selected = tab == CookingContentTab.Overview,
          onClick = { tab = CookingContentTab.Overview },
          text = { Text(text = "Overview") },
          modifier = Modifier
        )
        
        recipe.instructions.forEachIndexed { index, _ ->
          Tab(
            selected = tab == CookingContentTab.Instruction(index),
            onClick = { tab = CookingContentTab.Instruction(index) },
            text = { Text(text = "Step ${index + 1}") },
            modifier = Modifier
          )
        }
        
        Tab(
          selected = tab == CookingContentTab.Complete,
          onClick = { tab = CookingContentTab.Complete },
          text = { Text(text = "Complete") },
          modifier = Modifier
        )
      }
      
      LaunchedEffect(tab) {
        pagerState.animateScrollToPage(tab.index(recipe))
      }

      LaunchedEffect(pagerState.targetPage) {
        tab = CookingContentTab.fromIndex(index = pagerState.targetPage, recipe = recipe)
      }

      HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxWidth()
      ) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          when (tab) {
            CookingContentTab.Overview -> Text(text = "Overview")
            is CookingContentTab.Instruction -> Text(text = "Step ${(tab as CookingContentTab.Instruction).index + 1}")
            CookingContentTab.Complete -> Text(text = "Complete")
          }
        }
      }
    }
  }
}