package com.kronos.skilletapp.ui.screen.cooking

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@Composable
fun CookingContent(
  recipe: Recipe,
  selectedUnits: Map<Ingredient, MeasurementUnit?>,
  modifier: Modifier = Modifier
) {
  var tab by remember { mutableStateOf(CookingContentTab.Overview) }
  val pagerState = rememberPagerState { recipe.instructions.size + 2 }

  Surface(
    modifier = modifier,
    color = MaterialTheme.colorScheme.background
  ) {
    Column(modifier = Modifier.fillMaxSize()) {

    }
  }
}