package com.kronos.skilletapp

import android.app.Application
import com.kronos.skilletapp.ui.viewmodel.RecipePageViewModel
import com.kronos.skilletapp.data.RecipeRepository
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

class SkilletApp : Application() {

  override fun onCreate() {
    super.onCreate()

    startKoin {
      androidContext(this@SkilletApp)
      modules(appModule)
    }
  }
}

val appModule = module {
  singleOf(::RecipeRepository)
  viewModelOf(::RecipePageViewModel)
}