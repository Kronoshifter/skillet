package com.kronos.skilletapp.ui.screen.recipelist

import android.R.attr.label
import android.R.attr.onClick
import android.R.attr.singleLine
import android.webkit.URLUtil
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
import com.kronos.skilletapp.ui.KoinPreview
import com.kronos.skilletapp.ui.LoadingContent
import com.kronos.skilletapp.ui.dismiss
import com.kronos.skilletapp.ui.viewmodel.RecipeListViewModel
import com.leinardi.android.speeddial.compose.FabWithLabel
import com.leinardi.android.speeddial.compose.SpeedDial
import com.leinardi.android.speeddial.compose.SpeedDialOverlay
import com.leinardi.android.speeddial.compose.SpeedDialState
import kotlinx.coroutines.launch
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

  var showImportRecipeBottomSheet by remember { mutableStateOf(false) }

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
  ) { paddingValues ->
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    LoadingContent(
      state = uiState,
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) { data ->
      RecipeListContent(
        recipes = data.recipes,
        onRecipeClick = onRecipeClick,
        modifier = Modifier
          .fillMaxSize()
      )
    }

    SpeedDialOverlay(
      visible = overlayVisible,
      onClick = {
        overlayVisible = false
        speedDialState = speedDialState.toggle()
      },
    )

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    //TODO: convert to speed dial
    if (showImportRecipeBottomSheet) {
      ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { showImportRecipeBottomSheet = false },
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        ) {
          Text(
            text = "Import Recipe",
            style = MaterialTheme.typography.titleLarge
          )

          var url by remember { mutableStateOf("") }
          var isValidUrl = URLUtil.isValidUrl(url)

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
                  sheetState.dismiss(scope) { showImportRecipeBottomSheet = false }
                }

                keyboard?.hide()
              }
            ),
            singleLine = true,
            isError = !isValidUrl && url.isNotBlank(),
            supportingText = {
              if (!isValidUrl && url.isNotBlank()) {
                Text(
                  text = "Invalid URL",
                )
              }
            }
          )

          Button(
            onClick = {
              onNewRecipeByUrl(url)
              sheetState.dismiss(scope) { showImportRecipeBottomSheet = false }
            },
            enabled = isValidUrl,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(text = "Import")
          }
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
    contentPadding = PaddingValues(8.dp),
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
          .fillMaxSize()
      )
    }
  }
}