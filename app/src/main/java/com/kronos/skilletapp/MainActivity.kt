package com.kronos.skilletapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.kronos.skilletapp.ui.theme.SkilletAppTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.compose.KoinContext

class MainActivity : ComponentActivity() {
  private val intentFlow = MutableSharedFlow<Intent>(extraBufferCapacity = 1)

  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      KoinContext() {
        SkilletAppTheme {
          val navController = rememberNavController()
          val navActions = remember(navController) { SkilletNavigationActions(navController) }

          Scaffold(
            bottomBar = {
              SkilletBottomNavigationBar(
                navController = navController,
                navActions = navActions
              )
            },
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(TopAppBarDefaults.windowInsets)
          ) { padding ->
            SkilletNavGraph(
              intentFlow = intentFlow,
              modifier = Modifier
                .fillMaxSize()
                .padding(padding),
              navController = navController,
              navActions = navActions
            )
          }
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)

    intentFlow.tryEmit(intent)
  }
}