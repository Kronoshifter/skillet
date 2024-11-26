package com.kronos.skilletapp.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.room.Room
import com.kronos.skilletapp.appModule
import com.kronos.skilletapp.data.RecipeRepository
import com.kronos.skilletapp.data.SkilletError
import com.kronos.skilletapp.data.UiState
import com.kronos.skilletapp.database.RecipeDao
import com.kronos.skilletapp.database.RecipeDatabase
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.Instruction
import com.kronos.skilletapp.model.Measurement
import com.kronos.skilletapp.model.MeasurementUnit
import com.kronos.skilletapp.model.Recipe
import com.kronos.skilletapp.model.RecipeSource
import com.kronos.skilletapp.model.RecipeTime
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication
import org.koin.compose.KoinContext
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> LoadingContent(
  state: UiState<T>,
  modifier: Modifier = Modifier,
  loadingContent: @Composable () -> Unit = { CircularProgressIndicator() },
  errorContent: @Composable (SkilletError) -> Unit = { error -> Text(text = error.message) },
  content: @Composable (data: T) -> Unit,
) {
  AnimatedContent(
    targetState = state,
    label = "Loading",
    modifier = Modifier.fillMaxSize(),
  ) { targetState ->
    Box(
      contentAlignment = Alignment.Center, modifier = Modifier
        .fillMaxSize()
        .then(modifier)
    ) {
      when (targetState) {
        UiState.Loading -> loadingContent()
        is UiState.Error -> errorContent(targetState.error)
        is UiState.LoadedWithData -> content(targetState.data)
        UiState.Loaded -> throw IllegalStateException("Invalid state: $targetState")
      }
    }
  }
}

@Composable
fun LoadingContent(
  state: UiState<Nothing>,
  modifier: Modifier = Modifier,
  loadingContent: @Composable () -> Unit = { CircularProgressIndicator() },
  errorContent: @Composable (SkilletError) -> Unit = { error -> Text(text = error.message) },
  content: @Composable () -> Unit,
) {
  AnimatedContent(
    targetState = state,
    label = "Loading",
    modifier = Modifier.fillMaxSize(),
  ) { targetState ->
    Box(
      contentAlignment = Alignment.Center, modifier = Modifier
        .fillMaxSize()
        .then(modifier)
    ) {
      when (targetState) {
        UiState.Loading -> loadingContent()
        is UiState.Error -> errorContent(targetState.error)
        UiState.Loaded -> content()
        is UiState.LoadedWithData -> throw IllegalStateException("Invalid state: $targetState")
      }
    }
  }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RefreshingContent(
  refreshing: Boolean,
  onRefresh: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  val refreshState = rememberPullRefreshState(refreshing = refreshing, onRefresh = onRefresh)

  Box(
    modifier = Modifier
      .pullRefresh(refreshState)
      .then(modifier)
  ) {
    content()
    PullRefreshIndicator(refreshing = refreshing, state = refreshState, modifier = Modifier.align(Alignment.TopCenter))
  }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisableRipple(
  content: @Composable () -> Unit
) = CompositionLocalProvider(
  value = LocalRippleConfiguration provides null,
  content = content
)

@Composable
fun KoinPreview(
  content: @Composable () -> Unit
) {
  val context = LocalContext.current

  val previewModule = module {
    factory<Recipe> {
      val ingredients = listOf(
        Ingredient("Mini Shells Pasta", measurement = Measurement(8f, MeasurementUnit.Ounce), "8 oz Mini Shells Pasta"),
        Ingredient("Olive Oil", measurement = Measurement(1f, MeasurementUnit.Tablespoon), "1 tbsp Olive Oil"),
        Ingredient("Butter", measurement = Measurement(1f, MeasurementUnit.Tablespoon), "1 tbsp Butter"),
        Ingredient(
          name = "Garlic",
          measurement = Measurement(2f, MeasurementUnit.Custom("clove")),
          raw = "2 cloves Garlic"
        ),
        Ingredient("Flour", measurement = Measurement(2f, MeasurementUnit.Tablespoon), raw = "2 tbsp Flour"),
        Ingredient("Chicken Broth", measurement = Measurement(0.75f, MeasurementUnit.Cup), raw = "3/4 cup chicken broth"),
        Ingredient("Milk", measurement = Measurement(2.5f, MeasurementUnit.Cup), raw = "2 1/2 cups milk", comment = "separated"),
        Ingredient("Salt", measurement = Measurement(0f, MeasurementUnit.None), raw = "Salt, to taste", comment = "to taste"),
      )

      val instructions = listOf(
        Instruction(
          text = "Cook pasta in a pot of salted boiling water until al dente",
          ingredients = ingredients.take(1)
        ),
        Instruction(
          text = "Return pot to stove over medium heat then ass butter and olive oil. Once melted, add garlic then saute until light golden brown, about 30 seconds, being very careful not to burn. Sprinkle in flour then whisk and saute for 1 minute. Slowly pour in chicken broth and milk while whisking until mixture is smooth. Season with salt and pepper then switch to a wooden spoon and stir constantly until mixture is thick and bubbly, 4.-5 minutes.",
          ingredients = ingredients.slice(1..6)
        ),
        Instruction(
          text = "Remove pot from heat then stir in parmesan cheese, garlic powder, and parsley flakes until smooth. Add cooked pasta then stir to combine. Taste then adjust salt and pepper if necessary, and then serve.",
          ingredients = emptyList()
        ),
      )

      val recipe = Recipe(
        id = "test",
        name = "Creamy Garlic Pasta Shells",
        ingredients = ingredients,
        instructions = instructions,
        equipment = emptyList(),
        servings = 4,
        description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
        time = RecipeTime(15, 15),
        source = RecipeSource("My Brain", "My Brain"),
        notes = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
      )

      recipe
    }
  }

  KoinApplication(
    application = {
      androidContext(context)
      modules(previewModule)
    },
    content = content
  )
}