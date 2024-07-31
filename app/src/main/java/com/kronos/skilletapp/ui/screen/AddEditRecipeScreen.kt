package com.kronos.skilletapp.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kronos.skilletapp.model.Equipment
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.Instruction
import com.kronos.skilletapp.ui.LoadingContent
import com.kronos.skilletapp.ui.viewmodel.AddEditRecipeViewModel
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRecipeScreen(
  title: String,
  onBack: () -> Unit,
  onRecipeUpdate: (String) -> Unit,
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
  vm: AddEditRecipeViewModel = getViewModel(),
) {
  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          TextButton(onClick = vm::saveRecipe) {
            Text(text = "Save")
          }
        }
      )
    },
    snackbarHost = {
      SnackbarHost(
        hostState = snackbarHostState
      )
    }
  ) { paddingValues ->
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    LoadingContent(
      state = uiState,
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
    ) { data ->
      AddEditRecipeContent(
        name = data.name,
        description = data.description,
        notes = data.notes,
        servings = data.servings,
        prepTime = data.prepTime,
        cookTime = data.cookTime,
        source = data.source,
        sourceName = data.sourceName,
        ingredients = data.ingredients,
        instructions = data.instructions,
        equipment = data.equipment,
      )

      LaunchedEffect(data.isRecipeSaved) {
        if (data.isRecipeSaved) {
          onRecipeUpdate(vm.getRecipeId())
        }
      }

      data.userMessage?.let {
        LaunchedEffect(snackbarHostState, vm, it) {
          snackbarHostState.showSnackbar(it)
          vm.userMessageShown()
        }
      }
    }
  }
}

@Composable
fun AddEditRecipeContent(
  name: String,
  description: String,
  notes: String,
  servings: Int,
  prepTime: Int,
  cookTime: Int,
  source: String,
  sourceName: String,
  ingredients: List<Ingredient>,
  instructions: List<Instruction>,
  equipment: List<Equipment>,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier,
    color = MaterialTheme.colorScheme.background
  ) {
    Column(
      
    ) {

    }
  }
}