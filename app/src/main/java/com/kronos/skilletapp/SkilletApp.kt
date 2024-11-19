package com.kronos.skilletapp

import android.app.Application
import androidx.room.Room
import com.kronos.skilletapp.data.RecipeRepository
import com.kronos.skilletapp.database.RecipeDatabase
import com.kronos.skilletapp.ui.viewmodel.RecipeListViewModel
import com.kronos.skilletapp.ui.viewmodel.RecipeViewModel
import com.kronos.skilletapp.ui.viewmodel.AddEditRecipeViewModel
import com.kronos.skilletapp.ui.viewmodel.CookingViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

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

val appModule = module {
  singleOf(::RecipeRepository)
//  single { IngredientAiParser(androidContext()) }

  single<RecipeDatabase>(createdAtStart = true) {
    Room.databaseBuilder(
      context = androidContext(),
      klass = RecipeDatabase::class.java,
      name = "recipes.db"
    ).build()
  }

  viewModelOf(::RecipeListViewModel)
  viewModelOf(::RecipeViewModel)
  viewModelOf(::AddEditRecipeViewModel)
  viewModelOf(::CookingViewModel)
}