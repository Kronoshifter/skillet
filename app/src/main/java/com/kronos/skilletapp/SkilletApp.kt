package com.kronos.skilletapp

import android.app.Application
import android.content.Context
import androidx.room.Room
import coil3.ImageLoader
import coil3.request.crossfade
import com.kronos.skilletapp.data.RecipeRepository
import com.kronos.skilletapp.data.RecipeRepositoryImpl
import org.koin.dsl.bind
import com.kronos.skilletapp.database.RecipeDatabase
import com.kronos.skilletapp.domain.scaling.ScaleRecipe
import com.kronos.skilletapp.domain.scraping.ScrapeRecipe
import com.kronos.skilletapp.domain.validation.ValidateRecipe
import com.kronos.skilletapp.parser.IngredientParser
import com.kronos.skilletapp.scraping.DefaultRecipeScraper
import com.kronos.skilletapp.scraping.RecipeScraper
import com.kronos.skilletapp.ui.viewmodel.AddEditRecipeViewModel
import com.kronos.skilletapp.ui.viewmodel.CookingViewModel
import com.kronos.skilletapp.ui.viewmodel.RecipeListViewModel
import com.kronos.skilletapp.ui.viewmodel.RecipeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.withOptions
import org.koin.plugin.module.dsl.*
import org.koin.dsl.module
import org.koin.dsl.onClose

class SkilletApp : Application() {

  override fun onCreate() {
    super.onCreate()

    startKoin {
      androidLogger()
      androidContext(this@SkilletApp)
      modules(appModule)
    }
  }
}

private fun database(context: Context) = Room.databaseBuilder(
  context = context,
  klass = RecipeDatabase::class.java,
  name = "recipes.db",
).build()

private fun recipeDao(db: RecipeDatabase) = db.recipeDao()

private fun imageLoader(context: Context) = ImageLoader.Builder(context).crossfade(true).build()

val appModule = module {
  //  single { IngredientAiParser(androidContext()) }

  single { create(::database) } withOptions { createdAtStart() } onClose { it?.close() }
  single { create(::recipeDao) } withOptions { createdAtStart() }

  singleOf(::RecipeRepositoryImpl) { createdAtStart() } bind RecipeRepository::class

  singleOf(::IngredientParser)
  factoryOf(::DefaultRecipeScraper) bind RecipeScraper::class
  single { create(::imageLoader) } withOptions { createdAtStart() }

  factoryOf(::ScaleRecipe)
  factoryOf(::ScrapeRecipe)
  factoryOf(::ValidateRecipe)

  viewModelOf(::RecipeListViewModel)
  viewModelOf(::RecipeViewModel)
  viewModelOf(::AddEditRecipeViewModel)
  viewModelOf(::CookingViewModel)
}
