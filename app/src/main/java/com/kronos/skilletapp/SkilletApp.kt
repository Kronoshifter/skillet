package com.kronos.skilletapp

import android.app.Application
import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.async.executeSQL
import androidx.sqlite.driver.AndroidSQLiteDriver
import coil3.ImageLoader
import coil3.request.crossfade
import com.kronos.skilletapp.data.RecipeMapper
import com.kronos.skilletapp.data.RecipeRepository
import com.kronos.skilletapp.data.RecipeRepositoryImpl
import com.kronos.skilletapp.database.RecipeDatabase
import com.kronos.skilletapp.database.migrations.MIGRATION_1_2
import com.kronos.skilletapp.database.migrations.MIGRATION_2_3
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
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.dsl.onClose
import org.koin.plugin.module.dsl.*

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

// Enforces the entity @ForeignKey/@CASCADE constraints at open time. Room does not always force
// foreign-key enforcement on, so the pragma is set explicitly in an open callback.
private val fkPragmaCallback =
  object : RoomDatabase.Callback() {
    override suspend fun onOpen(connection: SQLiteConnection) {
      super.onOpen(connection)
      connection.executeSQL("PRAGMA foreign_keys = ON")
    }
  }

private fun database(context: Context) =
  Room.databaseBuilder(
      context = context,
      klass = RecipeDatabase::class.java,
      name = "recipes.db",
    )
    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
    .addCallback(fkPragmaCallback)
    .setQueryCoroutineContext(Dispatchers.IO)
    .setDriver(AndroidSQLiteDriver())
    .build()

private fun recipeDao(db: RecipeDatabase) = db.recipeDao()

private fun ingredientDao(db: RecipeDatabase) = db.ingredientDao()

private fun equipmentDao(db: RecipeDatabase) = db.equipmentDao()

private fun instructionDao(db: RecipeDatabase) = db.instructionDao()

private fun imageLoader(context: Context) = ImageLoader.Builder(context).crossfade(true).build()

val appModule = module {
  //  single { IngredientAiParser(androidContext()) }

  single { create(::database) } withOptions { createdAtStart() } onClose { it?.close() }
  single { create(::recipeDao) } withOptions { createdAtStart() }
  single { create(::ingredientDao) } withOptions { createdAtStart() }
  single { create(::equipmentDao) } withOptions { createdAtStart() }
  single { create(::instructionDao) } withOptions { createdAtStart() }

  singleOf(::RecipeMapper)
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
