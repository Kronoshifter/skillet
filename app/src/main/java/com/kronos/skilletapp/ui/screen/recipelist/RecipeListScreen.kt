package com.kronos.skilletapp.ui.screen.recipelist

import android.webkit.URLUtil.isValidUrl
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kronos.skilletapp.data.RecipeRepository
import com.kronos.skilletapp.model.Recipe
import com.kronos.skilletapp.ui.DisableRipple
import com.kronos.skilletapp.ui.FabPadding
import com.kronos.skilletapp.ui.KoinPreview
import com.kronos.skilletapp.ui.LoadingContent
import com.kronos.skilletapp.ui.component.ActionBottomSheet
import com.kronos.skilletapp.ui.component.SkilletBottomNavigationBar
import com.kronos.skilletapp.ui.dismiss
import com.kronos.skilletapp.ui.viewmodel.RecipeListViewModel
import com.kronos.skilletapp.utils.isNotNullOrBlank
import com.leinardi.android.speeddial.compose.FabWithLabel
import com.leinardi.android.speeddial.compose.SpeedDial
import com.leinardi.android.speeddial.compose.SpeedDialOverlay
import com.leinardi.android.speeddial.compose.SpeedDialState
import kotlinx.coroutines.runBlocking
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun RecipeListScreen(
  onNewRecipe: () -> Unit,
  onNewRecipeByUrl: (url: String) -> Unit,
  onRecipeClick: (id: String) -> Unit,
  vm: RecipeListViewModel = koinViewModel(),
) {
  var speedDialState by rememberSaveable { mutableStateOf(SpeedDialState.Collapsed) }
  var overlayVisible by rememberSaveable { mutableStateOf(speedDialState.isExpanded()) }

  var showImportRecipeBottomSheet by rememberSaveable { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(text = "Recipes") },
        actions = {
          //TODO: Implement search
//          IconButton(onClick = { /*TODO*/ }) {
//            Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
//          }

          IconButton(onClick = { /*TODO*/ }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More Options")
          }
        }
      )
    },
    bottomBar = {
      SkilletBottomNavigationBar()
    },
    floatingActionButton = {
      SpeedDial(
        state = speedDialState,
        onFabClick = {
          overlayVisible = !it
          speedDialState = speedDialState.toggle()
        },
        fabClosedContent = {
          Icon(imageVector = Icons.Default.Add, contentDescription = "Open Speed Dial")
        },
        fabOpenedContent = {
          Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
        },
      ) {
        item {
          FabWithLabel(
            onClick = {
              onNewRecipe()
              overlayVisible = false
              speedDialState = speedDialState.toggle()
            },
            labelContent = { Text(text = "Create new recipe") }
          ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Create new recipe")
          }
        }

        item {
          FabWithLabel(
            onClick = {
              showImportRecipeBottomSheet = true
              overlayVisible = false
              speedDialState = speedDialState.toggle()
            },
            labelContent = { Text(text = "Import from URL") }
          ) {
            Icon(imageVector = Icons.Default.Link, contentDescription = "Import from URL")
          }
        }
      }
    },
    floatingActionButtonPosition = FabPosition.End,
  ) { padding ->
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    LoadingContent(
      state = uiState,
      modifier = Modifier
        .fillMaxSize()
//        .padding(padding)
    ) { data ->
      RecipeListContent(
        recipes = data.recipes,
        onRecipeClick = onRecipeClick,
        modifier = Modifier
          .fillMaxSize(),
//          .padding(padding)
        gridPadding = PaddingValues(
          start = 8.dp,
          end = 8.dp,
          top = padding.calculateTopPadding() + 8.dp,
          bottom = padding.calculateBottomPadding() + FabPadding
        )
      )

      DisableRipple {
        SpeedDialOverlay(
          visible = overlayVisible,
          onClick = {
            overlayVisible = false
            speedDialState = speedDialState.toggle()
          },
        )
      }

      LaunchedEffect(vm.sharedRecipe) {
        showImportRecipeBottomSheet = vm.sharedRecipe?.url.isNotNullOrBlank() && vm.showSharedUrl
      }

      val sheetState = rememberModalBottomSheetState()
      val scope = rememberCoroutineScope()
      var url by remember { mutableStateOf(vm.sharedRecipe?.url?.takeIf { vm.showSharedUrl } ?: "") }

      if (showImportRecipeBottomSheet) {
        var isValidUrl = isValidUrl(url)

        ActionBottomSheet(
          sheetState = sheetState,
          modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
          onDismissRequest = {
            url = ""
            vm.showSharedUrl = false
            showImportRecipeBottomSheet = false
          },
          title = { Text(text = "Import Recipe") },
          action = {
            TextButton(
              onClick = {
                onNewRecipeByUrl(url)
                url = ""
                vm.showSharedUrl = false
                sheetState.dismiss(scope) { showImportRecipeBottomSheet = false }
              },
              enabled = isValidUrl
            ) {
              Text(text = "Import")
            }
          },
        ) {
          val keyboard = LocalSoftwareKeyboardController.current

          OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text(text = "URL") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, keyboardType = KeyboardType.Uri),
            keyboardActions = KeyboardActions(
              onDone = {
                if (isValidUrl) {
                  onNewRecipeByUrl(url)
                  url = ""
                  vm.showSharedUrl = false
                  sheetState.dismiss(scope) { showImportRecipeBottomSheet = false }
                }

                keyboard?.hide()
              }
            ),
            singleLine = true,
            isError = !isValidUrl && url.isNotBlank(),
            supportingText = {
              if (!isValidUrl && url.isNotBlank()) {
                Text(text = "Invalid URL")
              }
            }
          )
        }
      }
    }
  }
}

@Composable
private fun RecipeListContent(
  recipes: List<Recipe>,
  onRecipeClick: (id: String) -> Unit,
  modifier: Modifier = Modifier,
  gridPadding: PaddingValues = PaddingValues(8.dp),
) {
  if (recipes.isEmpty()) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
      Text(text = "No Recipes", color = MaterialTheme.colorScheme.secondary)
    }
    return
  }

  LazyVerticalGrid(
    columns = GridCells.Fixed(2),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    contentPadding = gridPadding,
    modifier = modifier
  ) {
    items(recipes) { recipe ->
      RecipeCard(
        recipe = recipe,
        onClick = { onRecipeClick(recipe.id) },
        modifier = Modifier
      )
    }
  }
}

@Composable
fun RecipeCard(
  recipe: Recipe,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    onClick = onClick,
    modifier = modifier
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      Text(
        text = recipe.name.first().uppercase(),
        style = MaterialTheme.typography.titleLarge,
        fontSize = 192.sp,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
        modifier = Modifier
          .align(Alignment.Center)
      )

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
          .height(IntrinsicSize.Max)
          .fillMaxWidth()
          .clip(CardDefaults.shape)
          .background(MaterialTheme.colorScheme.primary)
          .align(Alignment.BottomCenter)
      ) {
        Text(
          text = recipe.name,
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onPrimary,
          overflow = TextOverflow.Ellipsis,
          maxLines = 2,
          modifier = Modifier
            .padding(8.dp)
        )
      }
    }
  }
}

/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
//////////////////// PREVIEWS ///////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////

@Preview
@Composable
fun RecipeCardPreview() {
  KoinPreview {

    val repository = koinInject<RecipeRepository>()
    val recipe = runBlocking { repository.fetchRecipe("test") }

    RecipeCard(
      recipe = recipe,
      onClick = { },
      modifier = Modifier
        .aspectRatio(1f)
    )
  }
}

@Preview
@Composable
fun RecipeListPreview() {
  KoinPreview {

    val repository = koinInject<RecipeRepository>()
    val recipes = runBlocking { repository.fetchRecipes() }

    Surface {
      RecipeListContent(
        recipes = recipes,
        onRecipeClick = { },
        modifier = Modifier
          .fillMaxSize(),
        gridPadding = PaddingValues(8.dp)
      )
    }
  }
}