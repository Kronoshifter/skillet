package com.kronos.skilletapp.ui.screen.cooking

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.MeasurementUnit
import com.kronos.skilletapp.model.Recipe
import com.kronos.skilletapp.ui.LoadingContent
import com.kronos.skilletapp.ui.component.IngredientRow
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
  val recipeState by vm.recipeState.collectAsStateWithLifecycle()
  val uiState by vm.uiState.collectAsStateWithLifecycle()

  LoadingContent(
    state = recipeState,
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
        selectedUnits = uiState.selectedUnits,
        onUnitSelect = vm::selectUnit,
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues),
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CookingContent(
  recipe: Recipe,
  selectedUnits: Map<Ingredient, MeasurementUnit?>,
  onUnitSelect: (Ingredient, MeasurementUnit?) -> Unit,
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
            CookingContentTab.Overview -> OverviewContent(
              recipe = recipe,
              selectedUnits = selectedUnits,
              onUnitSelect = onUnitSelect
            )
            is CookingContentTab.Instruction -> Text(text = "Step ${(tab as CookingContentTab.Instruction).index + 1}")
            CookingContentTab.Complete -> Text(text = "Complete")
          }
        }
      }
    }
  }


}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OverviewContent(
  recipe: Recipe,
  selectedUnits: Map<Ingredient, MeasurementUnit?>,
  onUnitSelect: (Ingredient, MeasurementUnit?) -> Unit
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp)
  ) {
    Text(
      text = "Overview",
      style = MaterialTheme.typography.headlineLarge,
      modifier = Modifier
        .fillMaxWidth()
    )

    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      stickyHeader {
        Text(
          text = "Ingredients",
          style = MaterialTheme.typography.titleLarge,
          modifier = Modifier
            .fillMaxWidth()
        )
      }

      items(
        items = recipe.ingredients,
        key = { it.id }
      ) { ingredient ->
        //TODO: create reusable ingredient list item with unit selection
        //IngredientListItem(ingredient, selectedUnits[ingredient], onUnitSelect)
        Text("${ingredient.name} - ${ingredient.measurement.displayQuantity} ${ingredient.measurement.unit.name}")
      }

      if (recipe.equipment.isNotEmpty()) {
        stickyHeader {
          Text(
            text = "Equipment",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
              .fillMaxWidth()
          )
        }

        items(
          items = recipe.equipment,
          key = { it.id }
        ) { equipment ->
          Text(text = equipment.name)
        }
      }
    }
  }
}