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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.Instruction
import com.kronos.skilletapp.model.measurement.MeasurementUnit
import com.kronos.skilletapp.model.Recipe
import com.kronos.skilletapp.ui.LoadingContent
import com.kronos.skilletapp.ui.KoinPreview
import com.kronos.skilletapp.ui.component.IngredientListItem
import com.kronos.skilletapp.ui.theme.SkilletAppTheme
import com.kronos.skilletapp.ui.viewmodel.CookingViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

private sealed class CookingContentTab {
  data object Overview : CookingContentTab()
  data class Instruction(val instruction: Int) : CookingContentTab()
  data object Complete : CookingContentTab()

  fun index(recipe: Recipe): Int = when (this) {
    Overview -> 0
    is Instruction -> instruction + 1
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
  vm: CookingViewModel = koinViewModel(),
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
        scale = uiState.scale,
        selectedUnits = uiState.selectedUnits,
        onUnitSelect = vm::selectUnit,
        onBack = onBack,
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
  scale: Float,
  selectedUnits: Map<Ingredient, MeasurementUnit?>,
  onUnitSelect: (Ingredient, MeasurementUnit?) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var tab: CookingContentTab by remember { mutableStateOf(CookingContentTab.Overview) }
  val pagerState = rememberPagerState { recipe.instructions.size + 2 }

  Box(
    modifier = modifier,
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
        val page = CookingContentTab.fromIndex(it, recipe)

        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.TopCenter
        ) {
          when (page) {
            CookingContentTab.Overview -> OverviewTabContent(
              recipe = recipe,
              scale = scale,
              selectedUnits = selectedUnits,
              onUnitSelect = onUnitSelect
            )

            is CookingContentTab.Instruction -> InstructionTabContent(
              index = page.instruction,
              instruction = recipe.instructions[page.instruction],
              scale = scale,
              selectedUnits = selectedUnits,
              onUnitSelect = onUnitSelect
            )

            CookingContentTab.Complete -> CompleteTabContent(
              recipe = recipe,
              onBack = onBack,
              modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 16.dp)
            )
          }
        }
      }
    }
  }


}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OverviewTabContent(
  recipe: Recipe,
  scale: Float,
  selectedUnits: Map<Ingredient, MeasurementUnit?>,
  onUnitSelect: (Ingredient, MeasurementUnit?) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
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
      if (recipe.ingredients.isNotEmpty()) {
        stickyHeader {
          Text(
            text = "Gather your ingredients",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
              .fillMaxWidth()
          )
        }

        items(
          items = recipe.ingredients,
          key = { it.id }
        ) { ingredient ->
          var checked by rememberSaveable { mutableStateOf(false) }

          IngredientListItem(
            ingredient = ingredient,
            scale = scale,
            selectedUnit = selectedUnits[ingredient],
            onUnitSelect = onUnitSelect,
            checked = checked,
            onCheckedChange = { checked = it },
          )
        }
      }

      if (recipe.equipment.isNotEmpty()) {
        stickyHeader {
          Text(
            text = "Gather your equipment",
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

@Composable
fun InstructionTabContent(
  index: Int,
  instruction: Instruction,
  scale: Float,
  selectedUnits: Map<Ingredient, MeasurementUnit?>,
  onUnitSelect: (Ingredient, MeasurementUnit?) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
  ) {
    item {
      Text(
        text = instruction.text,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier
          .fillMaxWidth()
      )
    }

    //TODO: parse instruction for timers
    //TODO: implement recipe timer

    if (instruction.ingredients.isNotEmpty()) {
      item { HorizontalDivider() }

      item {
        Text(
          text = "Ingredients used in this step",
          style = MaterialTheme.typography.titleMedium,
          modifier = Modifier.fillMaxWidth()
        )
      }

      items(
        items = instruction.ingredients,
        key = { it.id }
      ) { ingredient ->
        var checked by rememberSaveable { mutableStateOf(false) }

        IngredientListItem(
          ingredient = ingredient,
          scale = scale,
          selectedUnit = selectedUnits[ingredient],
          onUnitSelect = onUnitSelect,
          checked = checked,
          onCheckedChange = { checked = it },
        )

        if (ingredient == instruction.ingredients.last()) {
          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }
  }
}

@Composable
fun CompleteTabContent(
  recipe: Recipe,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  // TODO: display cover or 'take a photo'

  Box(
    modifier = modifier
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.Center)
    ) {
      Text(
        text = "Enjoy your ${recipe.name}!",
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
      )

      Button(
        onClick = onBack
      ) {
        Text(text = "All Done!")
      }
    }

    // TODO: possibly allow for adding notes here
  }
}

/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
//////////////////// PREVIEWS ///////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////

@Preview
@Composable
fun OverviewContentPreview() {
  KoinPreview {
    val recipe = koinInject<Recipe>()

    val selectedUnits = remember { mutableStateMapOf<Ingredient, MeasurementUnit?>() }

    SkilletAppTheme {
      Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        OverviewTabContent(
          recipe = recipe,
          scale = 1f,
          selectedUnits = selectedUnits,
          onUnitSelect = { ingredient, unit -> selectedUnits[ingredient] = unit }
        )
      }
    }
  }
}

@Preview
@Composable
fun InstructionContentPreview() {
  KoinPreview {
    val recipe = koinInject<Recipe>()

    val selectedUnits = remember { mutableStateMapOf<Ingredient, MeasurementUnit?>() }

    SkilletAppTheme {
      Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        InstructionTabContent(
          index = 0,
          instruction = recipe.instructions.first(),
          scale = 1f,
          selectedUnits = selectedUnits,
          onUnitSelect = { ingredient, unit -> selectedUnits[ingredient] = unit }
        )
      }
    }
  }
}

@Preview
@Composable
fun CompleteContentPreview() {
  KoinPreview {
    val recipe = koinInject<Recipe>()

    SkilletAppTheme {
      Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        CompleteTabContent(
          recipe = recipe,
          onBack = {}
        )
      }
    }
  }
}