package com.kronos.skilletapp.ui.screen.recipelist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import kotlinx.coroutines.runBlocking
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
  onAddRecipe: () -> Unit,
  onRecipeClick: (id: String) -> Unit,
  vm: RecipeListViewModel = koinViewModel(),
) {
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
      FloatingActionButton(onClick = onAddRecipe) {
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
  }}

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
  }}