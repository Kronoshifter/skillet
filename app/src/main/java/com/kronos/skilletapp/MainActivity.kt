package com.kronos.skilletapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.util.Consumer
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import coil3.ImageLoader
import coil3.request.crossfade
import com.kronos.skilletapp.data.RecipeRepository
import com.kronos.skilletapp.database.RecipeDatabase
import com.kronos.skilletapp.navigation.LocalNavController
import com.kronos.skilletapp.navigation.LocalNavigationActions
import com.kronos.skilletapp.navigation.SkilletNavGraph
import com.kronos.skilletapp.navigation.SkilletNavigationActions
import com.kronos.skilletapp.parser.IngredientParser
import com.kronos.skilletapp.scraping.RecipeScraper
import com.kronos.skilletapp.ui.theme.SkilletAppTheme
import com.kronos.skilletapp.ui.viewmodel.AddEditRecipeViewModel
import com.kronos.skilletapp.ui.viewmodel.CookingViewModel
import com.kronos.skilletapp.ui.viewmodel.RecipeListViewModel
import com.kronos.skilletapp.ui.viewmodel.RecipeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.compose.KoinApplication
import org.koin.core.module.dsl.*
import org.koin.dsl.module

class MainActivity : ComponentActivity() {

  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    if (savedInstanceState == null) {
      Log.d("SHARED_RECIPE", "Cold Start")
      Log.d("SHARED_RECIPE", "Intent: ${intent.action}, ${intent.hasExtra(Intent.EXTRA_TEXT)}, ${intent.extras}")
    }

    setContent {
      SkilletAppTheme {
        KoinApplication(
          application = {
            androidLogger()
            androidContext(this@MainActivity)
            modules(appModule)
          }
        ) {
          val navController = rememberNavController()
          val navActions = remember(navController) { SkilletNavigationActions(navController) }

          val newIntent by produceState(intent.takeIf { it.action == Intent.ACTION_SEND }) {
            val consumer = Consumer<Intent> { intent ->
              Log.d("SHARED_RECIPE", "New Intent: ${intent.action}")
              value = intent
            }

            addOnNewIntentListener(consumer)
            awaitDispose {
              removeOnNewIntentListener(consumer)
            }
          }

          Surface {
            CompositionLocalProvider(
              LocalNavigationActions provides navActions,
              LocalNavController provides navController
            ) {
              SkilletNavGraph(
                newIntent = newIntent,
                navController = navController,
                navActions = navActions,
                modifier = Modifier
                  .fillMaxSize(),
              )
            }
          }
        }
      }
    }
  }
}

val appModule = module {
//  single { IngredientAiParser(androidContext()) }

  single {
    Room.databaseBuilder(
      context = androidContext(),
      klass = RecipeDatabase::class.java,
      name = "recipes.db"
    ).build()
  } withOptions {
    createdAtStart()
  }

  single { get<RecipeDatabase>().recipeDao() } withOptions {
    createdAtStart()
  }

  singleOf(::RecipeRepository) {
    createdAtStart()
  }

  singleOf(::IngredientParser)
  factoryOf(::RecipeScraper)
  single<ImageLoader> {
    ImageLoader.Builder(androidContext())
      .crossfade(true)
      .build()
  } withOptions {
    createdAtStart()
  }

  viewModelOf(::RecipeListViewModel)
  viewModelOf(::RecipeViewModel)
  viewModelOf(::AddEditRecipeViewModel)
  viewModelOf(::CookingViewModel)
}