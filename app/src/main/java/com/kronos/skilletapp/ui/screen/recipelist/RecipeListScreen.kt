package com.kronos.skilletapp.ui.screen.recipelist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kronos.skilletapp.data.RecipeRepository
import com.kronos.skilletapp.model.Recipe
import com.kronos.skilletapp.ui.LoadingContent
import com.kronos.skilletapp.ui.KoinPreview
import com.kronos.skilletapp.ui.viewmodel.RecipeListViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
  onNewRecipe: () -> Unit,
  onNewRecipeByUrl: (url: String) -> Unit,
  onRecipeClick: (id: String) -> Unit,
  vm: RecipeListViewModel = koinViewModel(),
) {
  var showAddRecipeBottomSheet by remember { mutableStateOf(false) }

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
      FloatingActionButton(onClick = { showAddRecipeBottomSheet = true }) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
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

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    //TODO: convert to speed dial
    if (showAddRecipeBottomSheet) {
      ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { showAddRecipeBottomSheet = false },
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        ) {
          Text(
            text = "Add Recipe",
            style = MaterialTheme.typography.titleLarge
          )

          Button(
            onClick = {
              onNewRecipe()

              scope.launch { sheetState.hide() }.invokeOnCompletion {
                if (!sheetState.isVisible) {
                  showAddRecipeBottomSheet = false
                }
              }
            },
            modifier = Modifier
          ) {
            Icon(imageVector = Icons.Default.AddCircleOutline, contentDescription = "Create new recipe")
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Create from scratch")
          }

          Button(
            onClick = {
//              onNewRecipeByUrl()
              //TODO: open dialog to enter url

              scope.launch { sheetState.hide() }.invokeOnCompletion {
                if (!sheetState.isVisible) {
                  showAddRecipeBottomSheet = false
                }
              }
            },
            modifier = Modifier
          ) {
            Icon(imageVector = Icons.Default.Link, contentDescription = "Add Link")
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Save recipe link")
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